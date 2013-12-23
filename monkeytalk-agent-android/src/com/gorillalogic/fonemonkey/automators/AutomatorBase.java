/*  MonkeyTalk - a cross-platform functional testing tool
    Copyright (C) 2012 Gorilla Logic, Inc.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>. */
package com.gorillalogic.fonemonkey.automators;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.graphics.Rect;
import android.view.View;

import com.gorillalogic.fonemonkey.Log;
import com.gorillalogic.fonemonkey.PropertyUtil;
import com.gorillalogic.fonemonkey.Recorder;
import com.gorillalogic.fonemonkey.exceptions.FoneMonkeyScriptFailure;
import com.gorillalogic.monkeytalk.automators.AutomatorConstants;

/**
 * @author sstern
 * 
 */
public abstract class AutomatorBase implements IAutomator {

	public static final String PROP_VALUE = "value";
	private String monkeyID = "";
	private Object component = null;

	protected static Pattern onListener = Pattern.compile(".*On(.*)Listener");

	@Override
	public boolean isHtmlAutomator() {
		return false;
	}

	@Override
	public void record(String operation, String... args) {
		Recorder.record(operation, this, args);
	}

	@Override
	public void record(String action, Object[] args) {

		String[] sargs = null;

		if (args != null && args.length > 0) {
			sargs = new String[args.length - 1];

			for (int i = 1; i < args.length; i++) {
				sargs[i - 1] = args[i].toString();
			}
		}

		record(action, sargs);
	}

	@Override
	// By default, we don't hide anybody
	public boolean hides(View view) {
		return false;
	}

	@Override
	public Class<?> getComponentClass() {
		return null;
	}

	@Override
	public void setMonkeyID(String id) {
		monkeyID = id;
	}

	@Override
	public String getMonkeyID() {
		return monkeyID;
	}

	@Override
	public abstract String getComponentType();

	@Override
	public Object getComponent() {
		return component;
	}

	@Override
	public void setComponent(Object o) {
		component = o;
	}

	@Override
	public String play(String action, String... args) {
		//If not valid Verify action, throw error.
		if(action.toLowerCase().startsWith(AutomatorConstants.ACTION_VERIFY.toLowerCase()) 
				&& !isValidVerifyAction(action)) {
			throw new IllegalArgumentException("Verify action \"" + action
					+ "\" not valid for component type \"" + getComponentType() + "\"");
		} else if (action.equalsIgnoreCase(AutomatorConstants.ACTION_VERIFY_IMAGE)) {
			return playVerifyImage();
		} else if (action.toLowerCase().startsWith(AutomatorConstants.ACTION_VERIFY.toLowerCase())
				|| action.equalsIgnoreCase(AutomatorConstants.ACTION_GET)) {
			if (args.length == 0) {
				if (action.equalsIgnoreCase(AutomatorConstants.ACTION_VERIFY_NOT)) {
					throw new FoneMonkeyScriptFailure(action + " " + getComponentType() + " "
							+ getMonkeyID() + " unexpectedly found after timeout.");

				} else if (action.equalsIgnoreCase(AutomatorConstants.ACTION_VERIFY)) {
					return "";
				}
			}

			String vtype;
			if (action.toLowerCase().endsWith("regex")) {
				vtype = "regex";
			} else if (action.toLowerCase().endsWith("wildcard")) {
				vtype = "wildcard";
			} else {
				vtype = "exact";
			}
			if (args.length == 0 && !(action.equalsIgnoreCase(AutomatorConstants.ACTION_GET))) {
				throw new IllegalArgumentException("Missing required argument specifying " + vtype
						+ " value for verification");
			}

			String expectedValue = args[0];
			String actualValue;
			String propertyPath;
			if (args.length == 1 || args.length > 1 && args[1].trim().length() == 0) {
				propertyPath = PROP_VALUE;
				actualValue = getValue();
			} else {
				propertyPath = args[1];
				if (propertyPath.startsWith(".")) {
					// Native property
					actualValue = getValue(propertyPath.trim().substring(1));
				} else if (propertyPath.equals(PROP_VALUE)) {
					actualValue = getValue();
				} else if (isArray(propertyPath)) {
					actualValue = getArrayItem(propertyPath);
				} else {
					try {
						actualValue = getProperty(propertyPath);
					} catch (Exception e) {
						// No built-in property of the specified name. See if there's a native one.
						String path = propertyPath.startsWith(".") ? propertyPath.substring(1)
								: propertyPath;
						actualValue = getValue(path);
					}
				}
			}

			String fmsg = (args.length == 3) ? ": " + args[2] : "";

			if (action.equalsIgnoreCase(AutomatorConstants.ACTION_GET)) {
				return actualValue;
			}

			boolean not = action.toLowerCase().startsWith(
					AutomatorConstants.ACTION_VERIFY_NOT.toLowerCase());

			if (vtype.equals("regex")
					&& actualValue.matches(expectedValue)
					|| (vtype.equals("wildcard") && PropertyUtil.wildcardMatch(expectedValue,
							actualValue))
					|| (vtype.equals("exact") && expectedValue.equals(actualValue))) {
				if (not) {
					fmsg = getComponentType() + " " + getMonkeyID() + " " + action + " "
							+ propertyPath + " : actualValue \"" + actualValue
							+ "\" should not match \"" + expectedValue + "\"" + fmsg;

					throw new FoneMonkeyScriptFailure(fmsg);
				} else {
					return "";
				}

			} else if (not) {
				return "";
			}

			fmsg = getComponentType() + " " + getMonkeyID() + " " + action + " " + propertyPath
					+ ": Expected \"" + expectedValue + "\" but found \"" + actualValue + "\""
					+ fmsg;
			;

			throw new FoneMonkeyScriptFailure(fmsg);

		}

		throw new IllegalArgumentException("Action \"" + action
				+ "\" not valid for component type \"" + getComponentType() + "\"");
	}

	protected String getProperty(String propertyPath) {
		throw new IllegalArgumentException("Unrecognized property \"" + propertyPath + "\" for "
				+ getComponentType());
	}

	private static DecimalFormat dec2 = new DecimalFormat("0.0");

	/**
	 * @return the component's current value for the supplied propertyPath expression.
	 */
	public String getValue(String propertyPath) {
		if (propertyPath.equals(PROP_VALUE)) {
			return getValue();
		}
		String capitalized = Character.toUpperCase(propertyPath.charAt(0))
				+ propertyPath.substring(1);
		String getter = "get" + capitalized;
		String iser = "is" + capitalized;

		Method[] m = getComponent().getClass().getMethods();

		for (int i = 0; i < m.length; i++) {

			if (m[i].getName().equals(getter) && m[i].getParameterTypes().length == 0) {
				try {
					Object result = m[i].invoke(getComponent(), (Object[]) null);
					if (result instanceof Number) {
						Double d = ((Number) result).doubleValue();
						return dec2.format(d);

					}
					return result == null ? "" : result.toString();
				} catch (Exception e) {
					throw new RuntimeException("Unexpected error executing " + getter);
				}

			}

			if (m[i].getName().equals(iser) && m[i].getParameterTypes().length == 0) {

				String result = null;
				try {
					result = ((Boolean) m[i].invoke(getComponent(), (Object[]) null)).toString();
					return result.toString();
				} catch (Exception e) {
					throw new RuntimeException("Unexpected error executing " + iser);
				}

			}

		}
		throw new RuntimeException("Unable to find property \"" + propertyPath + "\" for "
				+ getComponentType());
	}

	/**
	 * @return true if this automator handles this type or some subtype of this type
	 */
	public boolean forSubtypeOf(String type) {
		if (type.equals(getComponentType())
				|| (getAliases() != null && Arrays.asList(getAliases()).contains(type))) {
			return true;
		}

		Class<?> sup = this.getClass().getSuperclass();
		while (sup != AutomatorBase.class && IAutomator.class.isAssignableFrom(sup)) {

			try {
				if (((IAutomator) sup.newInstance()).getComponentType().equals(type)) {
					return true;
				}
			} catch (Exception e) {
				Log.log(e);
				return false;
			}
			sup = sup.getSuperclass();
		}
		return false;

	}

	public String[] getAliases() {
		return null;
	}

	public String getValue() {
		return null;
	}

	/**
	 * Chains listeners implementing OnXxxListener interfaces. To chain a listener for a component,
	 * simply implement the interface on your automator. For example, if your automator subclass
	 * implements OnFocusChangeListener, then its onFocusChanged method will be called prior to
	 * calling the view's actual listener (if any was set).
	 * 
	 */
	@Override
	public boolean installDefaultListeners() {
		Class<?> sup = this.getClass();
		while (sup != null && IAutomator.class.isAssignableFrom(sup)) {
			Class<?>[] xfaces = sup.getInterfaces();
			for (Class<?> xface : xfaces) {
				if (onListener.matcher(xface.getName()).matches() && !isExcludedFromChaining(xface)) {
					chainListenerFor(xface);
				}

			}
			sup = sup.getSuperclass();
		}
		return true;
	}

	/**
	 * Subclasses can override to prevent listeners being created for an interface. Spinner needs to
	 * do this to prevent onClick listener being added.
	 * 
	 * @param xface
	 * @return true if no default handlers should be added for this interface
	 */
	protected boolean isExcludedFromChaining(Class<?> xface) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * used to warn about incumbent listeners for certain defaults
	 * 
	 * @param klass
	 *            - the listenre class which was found to be incumbent
	 * @param v
	 *            - the view that had it installed
	 */
	// protected static void logWarn(String klass, View v) {
	// Log.log("WARNING: You have a "
	// + klass
	// + " set on "
	// + v
	// + ". FoneMonkey needs to set its own listener "
	// + "in order to learn about any views added after onCreate() has finished."
	// + " See FoneMonkey's Multiplexed" + klass
	// + " class for a way to get around this problem.");
	// }

	protected static boolean isAndroidBuiltin(Class<?> klass) {
		if (klass != null) {
			return klass.getName().startsWith("android.")
					|| klass.getName().startsWith("com.android");
		}
		return false;
	}

	protected static boolean isAndroidBuiltin(Object o) {
		if (o != null) {
			return isAndroidBuiltin(o.getClass());
		}
		return false;
	}

	public void assertArgCount(String action, String[] args, int required) {
		if (args.length < required) {
			throw new IllegalArgumentException(action + " requires " + required
					+ " arguments, but found " + args.length);
		}
	}

	public void assertMaxArg(int arg, int max) {
		if (arg > max) {
			throw new IllegalArgumentException("Argument value " + arg + " " + "exceeds maximum "
					+ max);
		}
	}

	public int getIndexArg(String action, String arg) {
		return getIntegerArg(action, arg, 1);

	}

	public int getIntegerArg(String action, String arg, int min) {
		int i;
		try {
			i = Integer.valueOf(arg);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(action + " requires integer argument, but found \""
					+ arg + "\"");
		}

		if (i < min) {
			throw new IllegalArgumentException(action + " requires integer argument greater than "
					+ min + " , but found \"" + arg + "\"");
		}
		return i;
	}

	public int getOrdinal() {
		return 1;
	}

	protected String getListenerName(Class<?> listenerClass) {
		String[] parts = listenerClass.getCanonicalName().split("\\.");
		String listenerName = parts[parts.length - 1];
		return listenerName;
	}

	protected void chainListenerFor(Class<?> listenerClass) {
		String listenerName = getListenerName(listenerClass);
		String setterName = "set" + listenerName;
		chainListenerFor(listenerClass, setterName);

	}

	protected void chainListenerFor(Class<?> listenerClass, String setterName) {
		Class<?> klass = getComponent().getClass();
		String listenerName = getListenerName(listenerClass);
		String listenerField = "m" + listenerName;
		Object listener = null;
		Object target = getComponent();

		boolean found = false;
		Field f = null;
		while (!found) {
			try {
				try {
					f = klass.getDeclaredField(listenerField);
					f.setAccessible(true);
					listener = f.get(target);
				} catch (NoSuchFieldException e) {
					// listeners moved into inner ListenerInfo class in Android 4.0.3.
					Method m = klass.getDeclaredMethod("getListenerInfo", (Class<?>[]) null);
					m.setAccessible(true);
					target = m.invoke(target, (Object[]) null);
					klass = target.getClass();
					f = klass.getDeclaredField(listenerField);
					f.setAccessible(true);
					listener = f.get(target);
				}

				found = true;
			} catch (Exception e) {
				klass = klass.getSuperclass();
				if (klass == null) {
					throw new IllegalStateException("Unable to find field " + listenerField
							+ " in any superclass of " + target.getClass().getName());
				}

			}
		}

		if (listener instanceof Proxy
				&& Proxy.getInvocationHandler(listener) instanceof MonkeyInvocationHandler) {
			// Already chained. Should probably cache this fact somewhere.
			return;
		}

		Object proxy = Proxy.newProxyInstance(listenerClass.getClassLoader(),
				new Class<?>[] { listenerClass }, new MonkeyInvocationHandler(listener));

		// Calling the setter can result in Android PassthroughClickListener referencing us and vice
		// versa (and subsequent stackoverflow)
		// So we've got to assign the private field directly
		// if (setterName == null) {
		try {
			f.set(target, proxy);
			return;
		} catch (Exception e) {
			throw new IllegalStateException("Unable to assign field " + f.getName() + ": "
					+ e.getMessage());
		}
		// }
		// Method meth;
		// try {
		// meth = klass.getDeclaredMethod(setterName, listenerClass);
		// } catch (Exception e) {
		// throw new IllegalStateException("Unable to find setter for "
		// + listenerName + ": " + e.getMessage());
		// }
		// try {
		// meth.invoke(getComponent(), proxy);
		// } catch (Exception e) {
		// // if (e.getCause() != null
		// // && e.getCause().getMessage().contains("cannot be used")) { //
		// // Unfortunately, components throw indistinguishable runtime
		// // exceptions if a listener is set that they don't support (even
		// // though they have the setter
		// // Probably a subclass that doesn't support a listener defined
		// // on a superclass.
		// // eg, Spinner is a subclass of AdapterView but throws a
		// // RuntimeException if you
		// // try to set its onItemClickListener.
		// //
		// // Should probably cache so we don't repeatedly wind up here.
		// //
		// return;
		// // }
		// // throw new IllegalStateException("Error invoking " +
		// // meth.getName()
		// // + ": " + e.getMessage());
		// }

	}

	/**
	 * Intercepts method calls for recording by calling the impl'd listener interface on
	 * corresponding IAutomator, before calling the actual listener, if any.
	 * 
	 * @author sstern
	 * 
	 */
	private final class MonkeyInvocationHandler implements InvocationHandler {
		/**
		 * 
		 */
		private final Object listener;

		/**
		 * @param l
		 */
		private MonkeyInvocationHandler(Object listener) {
			this.listener = listener;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			// Call the automator's listener impl
			Object rc = method.invoke(AutomatorBase.this, args);
			if (listener == null) {
				return rc;
			}
			// Call the actual listener
			return method.invoke(listener, args);
		}
	}

	@Override
	public boolean canAutomate(String componentType, String monkeyID) {
		if (forSubtypeOf(componentType)) {
			if (getIdentifyingValues().contains(monkeyID) // using app-level ids
					|| matchesOrdinal(monkeyID)) { // using explicit or implicit ordinal
				return true;
			}
		}
		return false;
	}

	private boolean matchesOrdinal(String monkeyID) {
		if (monkeyID == null || monkeyID.trim().length() == 0 || monkeyID.equals("*")) {
			monkeyID = "#1";
		}
		return monkeyID.equals("#" + getOrdinal());
	}

	public List<String> getIdentifyingValues() {
		ArrayList<String> list = new ArrayList<String>();
		String id = getMonkeyID();
		if (id != null) {
			list.add(id);
		}
		return list;
	}

	// matches arrayName[n]...
	private static Pattern arrayPattern = Pattern.compile("^(\\w+)\\([\\d,]+\\)$");
	// matches [n]
	private static Pattern arrayIndexPattern = Pattern.compile("\\((\\d+)[,\\)]|(\\d+)[,\\)]");

	private boolean isArray(String propertyPath) {
		return arrayPattern.matcher(propertyPath).matches();
	}

	private String getArrayItem(String propertyPath) {
		Matcher m = arrayPattern.matcher(propertyPath);
		m.find();
		String name = m.group(1);
		ArrayList<Integer> indices = new ArrayList<Integer>();
		m = arrayIndexPattern.matcher(propertyPath);
		int group = 1;
		while (m.find()) {
			String s = m.group(group);
			int i = getIndexArg(AutomatorConstants.ACTION_GET, s);
			indices.add(i - 1);
			group++;
		}
		return getArrayItem(name, indices);

	}

	/**
	 * Subclasses should override to return array valued property items
	 * 
	 * @param arrayName
	 *            - the name of the array, eg, items
	 * @param indices
	 *            - A list containing (zero-based) index value for each dimension of the array
	 * @return the item at the specified indices
	 */
	protected String getArrayItem(String arrayName, List<Integer> indices) {
		throw new IllegalArgumentException(getComponentType() + " has no such array: " + arrayName);
	}

	/**
	 * performs the agent-side part of the verifyImage command take a screenshot and return it,
	 * along with the position and size of the target component
	 * 
	 * @return
	 */
	private String playVerifyImage() {
		Rect br = getBoundingRectangle();
		return takeScreenshot(br.left + " " + br.top + " " + br.width() + " " + br.height());
	}

	private String takeScreenshot(String msg) {
		if (msg == null) {
			msg = "no message";
		}
		Log.log("VerifyImage SCREENSHOT - " + msg + " - taking screenshot...");
		DeviceAutomator device = (DeviceAutomator) AutomationManager.findAutomatorByType("Device");

		if (device != null) {
			try {
				String screenshot = device.play(AutomatorConstants.ACTION_SCREENSHOT);
				if (screenshot != null && screenshot.startsWith("{screenshot")) {
					Log.log("SCREENSHOT - done!");
					return "{message:\"" + msg.replaceAll("\"", "'") + "\","
							+ screenshot.substring(1);
				}
			} catch (Exception ex) {
				String exMsg = ex.getMessage();
				if (exMsg != null) {
					exMsg = exMsg.replaceAll("\"", "'");
				} else {
					exMsg = ex.getClass().getName();
				}
				return msg + " -- " + exMsg;
			}
		}
		return msg;
	}

	protected Rect getBoundingRectangle() {
		return new Rect(0, 0, 1, 1);
	}
	
	private boolean isValidVerifyAction(String action) {
		return (action.equalsIgnoreCase(AutomatorConstants.ACTION_VERIFY_IMAGE)
				|| action.equalsIgnoreCase(AutomatorConstants.ACTION_VERIFY)
				|| action.equalsIgnoreCase(AutomatorConstants.ACTION_VERIFY_NOT)
				|| action.equalsIgnoreCase(AutomatorConstants.ACTION_VERIFY_REGEX)
				|| action.equalsIgnoreCase(AutomatorConstants.ACTION_VERIFY_NOT_REGEX)
				|| action.equalsIgnoreCase(AutomatorConstants.ACTION_VERIFY_WILDCARD)
				|| action.equalsIgnoreCase(AutomatorConstants.ACTION_VERIFY_NOT_WILDCARD));
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [" + getComponentType() + "]";
	}
}