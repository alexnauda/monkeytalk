package com.gorillalogic.monkeytalk.processor.command;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;

import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.processor.PlaybackListener;
import com.gorillalogic.monkeytalk.processor.PlaybackResult;
import com.gorillalogic.monkeytalk.processor.PlaybackStatus;
import com.gorillalogic.monkeytalk.processor.Scope;
import com.gorillalogic.monkeytalk.processor.ScriptProcessor;
import com.gorillalogic.monkeytalk.sender.Response;
import com.gorillalogic.monkeytalk.sender.Response.ResponseStatus;
import com.gorillalogic.monkeytalk.utils.Base64;
import com.gorillalogic.monkeytalk.utils.ImageUtils;

public class VerifyImage extends BaseCommand {
	ScriptProcessor scriptProcessor = null;
	File rootDir;
	String errorPrepend;

	public VerifyImage(Command cmd, Scope scope, PlaybackListener listener,
	        ScriptProcessor scriptProcessor, File rootDir) {
		super(cmd, scope, listener);
		this.scriptProcessor = scriptProcessor;
		this.rootDir = rootDir;
	}

	public PlaybackResult verifyImage() {

		if (cmd.getArgs().size() == 0 || cmd.getArgs().get(0).length() == 0) {
			return new PlaybackResult(PlaybackStatus.ERROR, "command '" + cmd.getCommand()
			        + "' must have a file path as its first arg", scope);
		}
		boolean shouldCreateExpectedFile = false;

		// validate file
		String expectedFilePath = cmd.getArgs().get(0);
		if (new File(expectedFilePath).isAbsolute()) {
			return new PlaybackResult(
			        PlaybackStatus.ERROR,
			        "command '"
			                + cmd.getCommand()
			                + "' - expectedImageFile '"
			                + expectedFilePath
			                + "' is an absolute path reference"
			                + "' - the expected image file path must be specified relative to the project directory",
			        scope);
		}
		File expectedFile = new File(rootDir, expectedFilePath);
		if (!expectedFile.exists()) {
			try {
				File expectedFileParent = expectedFile.getParentFile();
				if (!expectedFileParent.exists()) {
					expectedFileParent.mkdirs();
				}
				expectedFile.createNewFile();
				shouldCreateExpectedFile = true;
				expectedFile.delete();
			} catch (IOException e) {
				return new PlaybackResult(PlaybackStatus.ERROR, "command '" + cmd.getCommand()
				        + "' - expectedImageFile '" + expectedFilePath + "'"
				        + " does not exist and cannot be created: " + e.getMessage(), scope);
			}
		} else {
			if (!expectedFile.isFile()) {
				return new PlaybackResult(PlaybackStatus.ERROR, "command '" + cmd.getCommand()
				        + "' - expectedImageFile '" + expectedFilePath + "'"
				        + " is not a regular file, perhaps a folder?", scope);
			}
		}

		// optional tolerance
		int tolerance = ImageUtils.DEFAULT_TOLERANCE;
		if (cmd.getArgs().size() > 1) {
			boolean badTolerance = false;
			try {
				tolerance = Integer.parseInt(cmd.getArgs().get(1));
			} catch (NumberFormatException nfe) {
				badTolerance = true;
			}
			if (!badTolerance) {
				if (tolerance < ImageUtils.MIN_TOLERANCE || tolerance > ImageUtils.MAX_TOLERANCE) {
					badTolerance = true;
				}
			}
			if (badTolerance) {
				return new PlaybackResult(PlaybackStatus.ERROR, "command '" + cmd.getCommand()
				        + "' - tolerance '" + cmd.getArgs().get(1)
				        + "' is invalid: must be an integer" + " between "
				        + ImageUtils.MIN_TOLERANCE + " and " + ImageUtils.MAX_TOLERANCE, scope);
			}
		}

		listener.onStart(scope);
		PlaybackResult result = doVerifyImage(expectedFile, shouldCreateExpectedFile, tolerance);

		ResponseStatus responseStatus = ResponseStatus.OK;
		if (result.getStatus().equals(PlaybackStatus.ERROR)) {
			responseStatus = ResponseStatus.ERROR;
		} else if (result.getStatus().equals(PlaybackStatus.FAILURE)) {
			responseStatus = ResponseStatus.FAILURE;
		}
		Response resp = new Response(responseStatus, result.getMessage(), result.getWarning(),
		        result.getImage());
		// log("listener: class=" + listener.getClass().getName());
		listener.onComplete(scope, resp);

		return result;
	}

	public PlaybackResult doVerifyImage(File expectedFile, boolean shouldCreateExpectedFile,
	        int tolerance) {
		// agent will take a screenshot and also return the
		// rectangle of the target component in the message
		Response resp = scriptProcessor.runCommand(cmd);

		result = new PlaybackResult(resp, scope);
		if (!result.getStatus().equals(PlaybackStatus.OK)) {
			String errorMessage = "command '" + cmd.getCommand() + "'";
			String resultMessage = result.getMessage();
			if (resultMessage == null || resultMessage.length() == 0) {
				errorMessage += " - no message from agent";
			} else {
				if (!resultMessage.contains(errorMessage)) {
					errorMessage += " - " + resultMessage;
				} else {
					errorMessage = resultMessage;
				}
			}
			result.setMessage(errorMessage);
			return result;
		}

		String expectedFilePath = expectedFile.getPath();
		byte[] image = getImageForVerify(result);
		if (result.getStatus().equals(PlaybackStatus.ERROR)) {
			return result;
		}

		if (shouldCreateExpectedFile) {
			try {
				FileUtils.writeByteArrayToFile(expectedFile, image);
			} catch (IOException e) {
				result.setStatus(PlaybackStatus.ERROR);
				result.setMessage("command '" + cmd.getCommand()
				        + "' - error creating image file: " + e.getMessage());
			}
			String msg = "command '" + cmd.getCommand() + "' - file '" + expectedFilePath
			        + "' was not found, creating it with the just-captured image";
			result.setWarning(result.getWarning() == null ? msg : result.getWarning() + "; " + msg);
			result.setMessage(msg);
		} else {
			byte[] expectedImage = null;
			try {
				expectedImage = FileUtils.readFileToByteArray(expectedFile);
			} catch (IOException e) {
				result.setStatus(PlaybackStatus.ERROR);
				result.setMessage("command '" + cmd.getCommand() + "' - error reading image file '"
				        + expectedFilePath + "': " + e.getMessage());
				return result;
			}

			if (expectedImage == null || expectedImage.length == 0) {
				result.setStatus(PlaybackStatus.ERROR);
				result.setMessage("command '" + cmd.getCommand() + "' - expected image file '"
				        + expectedFilePath + "' was empty or could not be read.");
				return result;
			}

			// at last
			try {
				if (!compareImages(expectedImage, image, tolerance)) {
					result.setStatus(PlaybackStatus.FAILURE);
					result.setMessage("command '" + cmd.getCommand()
					        + "' - expected and captured images do not match.");
				} else {
					// YAY!
					result.setStatus(PlaybackStatus.OK);
					result.setMessage("");
				}
			} catch (IOException e) {
				result.setStatus(PlaybackStatus.ERROR);
				result.setMessage("command '" + cmd.getCommand()
				        + "' - error comparing expected and actual images: " + e.getMessage());
			}
		}
		return result;
	}

	private byte[] getImageForVerify(PlaybackResult result) {
		String imageStr = result.getImage();
		if (imageStr == null || imageStr.length() == 0) {
			result.setStatus(PlaybackStatus.ERROR);
			result.setMessage("command '" + cmd.getCommand() + "' - no screenshot received");
			return null;
		}

		byte[] image = null;
		try {
			image = Base64.decode(imageStr);
			System.out.println("imageStr length=" + imageStr.length() + " chars, decodes to "
			        + image.length + " bytes");
		} catch (IOException e) {
			result.setStatus(PlaybackStatus.ERROR);
			result.setMessage("command '" + cmd.getCommand() + "' - error decoding image: "
			        + e.getMessage());
			return null;
		}

		// get bounding rectangle if any, and clip image
		String agentMessage = result.getMessage();
		if (agentMessage != null && agentMessage.length() > 0) {
			boolean shouldWarn = false;
			// x, y, w, h
			String[] dimensions = agentMessage.split(" ");
			if (dimensions.length == 4) {
				try {

					int x = Integer.parseInt(dimensions[0]);
					int y = Integer.parseInt(dimensions[1]);
					int w = Integer.parseInt(dimensions[2]);
					int h = Integer.parseInt(dimensions[3]);
					
					if(x < 0 || y < 0 || w < 0 || h < 0) { //make sure dimensions are valid
						result.setStatus(PlaybackStatus.ERROR);
						result.setMessage("\"" + cmd.getComponentType() + "\" with MonkeyID, \"" + cmd.getMonkeyId() + "\" has invalid dimensions!");
						return null;
					}

					image = cropImage(image, x, y, w, h);
					if (image == null) {
						result.setStatus(PlaybackStatus.ERROR);
						result.setMessage("command '" + cmd.getCommand()
						        + "' - error cropping image");
						return null;
					}

				} catch (NumberFormatException nfe) {
					shouldWarn = true;
				}
			} else {
				shouldWarn = true;
			}
			if (shouldWarn) {
				String msg = "could not parse component rectangle from this string: " + dimensions;
				result.setWarning(result.getWarning() == null ? msg : result.getWarning() + "; "
				        + msg);
			}
		}
		return image;
	}

	private byte[] cropImage(byte[] image, int x, int y, int w, int h) {
		try {
			BufferedImage uncroppedImage = ImageIO.read(new ByteArrayInputStream(image));
			// log("uncropped: width=" + uncroppedImage.getWidth() + " height=" +
			// uncroppedImage.getHeight());
			if (uncroppedImage == null) {
				log("invalid screenshot - image could not be created");
				return null;
			}
			BufferedImage croppedImage = ImageUtils.cropImage(uncroppedImage, x, y, w, h);
			// log("cropped: width=" + croppedImage.getWidth() + " height=" +
			// croppedImage.getHeight());
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(croppedImage, "png", baos);
			image = baos.toByteArray();
			// log("cropped image has length: " + image.length);
		} catch (IOException e) {
			log("error creating image from supplied bitmap: " + e.getMessage());
			return null;
		}
		return image;
	}

	private boolean compareImages(byte[] expected, byte[] actual, int tolerance) throws IOException {
		log("comparing expectedImage of length: " + expected.length
		        + " with actualImage of length: " + actual.length);
		BufferedImage expectedImage = ImageIO.read(new ByteArrayInputStream(expected));
		BufferedImage actualImage = ImageIO.read(new ByteArrayInputStream(actual));
		return ImageUtils.compare(expectedImage, actualImage, tolerance);
	}

}
