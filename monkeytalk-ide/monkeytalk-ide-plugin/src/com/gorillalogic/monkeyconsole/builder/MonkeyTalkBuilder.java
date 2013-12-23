package com.gorillalogic.monkeyconsole.builder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.xml.sax.helpers.DefaultHandler;

import com.gorillalogic.monkeytalk.api.js.tools.JSLibGenerator;

public class MonkeyTalkBuilder extends IncrementalProjectBuilder {

	class MonkeyTalkDeltaVisitor implements IResourceDeltaVisitor {
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.
		 * IResourceDelta)
		 */
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				// handle added resource
				checkMonkeyTalk(resource);
				break;
			case IResourceDelta.REMOVED:
				checkMonkeyTalk(resource);
				break;
			case IResourceDelta.CHANGED:
				checkMonkeyTalk(resource);
				break;
			}
			// return true to continue visiting children.
			return true;
		}
	}

	class MonkeyTalkResourceVisitor implements IResourceVisitor {
		public boolean visit(IResource resource) {
			checkMonkeyTalk(resource);
			// return true to continue visiting children.
			return true;
		}
	}

	// This is just a class stub to be fixed later
	class MonkeyTalkException extends Exception {
		public int getLineNumber() {
			return 0;
		}
	}

	class MonkeyTalkParser {
	}

	class MonkeyTalkErrorHandler extends DefaultHandler {

		private IFile file;

		public MonkeyTalkErrorHandler(IFile file) {
			this.file = file;
		}

		private void addMarker(MonkeyTalkException e, int severity) {
			MonkeyTalkBuilder.this.addMarker(file, e.getMessage(), e.getLineNumber(), severity);
		}

		public void error(MonkeyTalkException exception) {
			addMarker(exception, IMarker.SEVERITY_ERROR);
		}

		public void fatalError(MonkeyTalkException exception) {
			addMarker(exception, IMarker.SEVERITY_ERROR);
		}

		public void warning(MonkeyTalkException exception) {
			addMarker(exception, IMarker.SEVERITY_WARNING);
		}
	}

	public static final String BUILDER_ID = "com.gorillalogic.monkeyconsole.builder.monkeyTalkBuilder";

	private static final String MARKER_TYPE = "com.gorillalogic.monkeyconsole.builder.monkeyTalkProblem";

	private void addMarker(IFile file, String message, int lineNumber, int severity) {
		try {
			IMarker marker = file.createMarker(MARKER_TYPE);
			marker.setAttribute(IMarker.MESSAGE, message);
			marker.setAttribute(IMarker.SEVERITY, severity);
			if (lineNumber == -1) {
				lineNumber = 1;
			}
			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
		} catch (CoreException e) {
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int, java.util.Map,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		if (kind == FULL_BUILD) {
			fullBuild(monitor);
		} else {
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				fullBuild(monitor);
			} else {
				incrementalBuild(delta, monitor);
			}
		}
		return null;
	}

	void checkMonkeyTalk(IResource resource) {
		if (resource instanceof IFile && resource.getName().endsWith(".mt")) {
			IFile file = (IFile) resource;
			deleteMarkers(file);
			reginLib(file);
			MonkeyTalkErrorHandler reporter = new MonkeyTalkErrorHandler(file);
			// try {
			// getParser().parse(file.getContents(), reporter);
			// } catch (Exception e1) {
			// }
		}
		if (resource instanceof IFile && resource.getName().endsWith(".mts")) {
			IFile file = (IFile) resource;
			deleteMarkers(file);
			MonkeyTalkErrorHandler reporter = new MonkeyTalkErrorHandler(file);
			reginLib(file);
			// try {
			// getParser().parse(file.getContents(), reporter);
			// } catch (Exception e1) {
			// }
		}
	}

	private void reginLib(IFile file) {
		// compute the parent folder, and the libs folder
		File dir = new File(file.getRawLocationURI().getRawPath().toString()).getParentFile();
		File libs = new File(dir, "libs");

		// first, we regen the lib
		try {
			String jsLIB = JSLibGenerator.createLib(file.getProject().getName(), dir);
			FileUtils.writeStringToFile(new File(libs, file.getProject().getName() + ".js"), jsLIB);
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		// second, we re-copy the API (just in case they upgraded)
		try {
			InputStream is = Thread.currentThread().getContextClassLoader()
					.getResourceAsStream("templates/MonkeyTalkAPI.js");
			IOUtils.copy(is, new FileOutputStream(new File(libs, "/MonkeyTalkAPI.js")));
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		// last, we refresh the workspace to pick everything up
		try {
			file.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			// Could not refresh workspace, no biggie
		}
	}

	private void deleteMarkers(IFile file) {
		try {
			file.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO);
		} catch (CoreException ce) {
		}
	}

	protected void fullBuild(final IProgressMonitor monitor) throws CoreException {
		try {
			getProject().accept(new MonkeyTalkResourceVisitor());
		} catch (CoreException e) {
		}
	}

	private MonkeyTalkParser getParser() {
		// TODO
		return null;
	}

	protected void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor)
			throws CoreException {
		// the visitor does the work.
		delta.accept(new MonkeyTalkDeltaVisitor());
	}
}
