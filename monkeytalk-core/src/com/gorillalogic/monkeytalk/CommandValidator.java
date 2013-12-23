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
package com.gorillalogic.monkeytalk;

/**
 * Class returned from the command validation process with a status and a
 * message.
 */
public class CommandValidator {
	private CommandStatus status;
	private String message;

	/**
	 * Instantiate a CommandValidator with an OK status.
	 */
	public CommandValidator() {
		this(CommandStatus.OK, null);
	}

	/**
	 * Instantiate a CommandValidator with the given status.
	 * 
	 * @param status
	 *            the status
	 */
	public CommandValidator(CommandStatus status) {
		this(status, null);
	}

	/**
	 * Instantiate a CommandValidator with the given status and message.
	 * 
	 * @param status
	 *            the status
	 * @param message
	 *            the error message
	 */
	public CommandValidator(CommandStatus status, String message) {
		setStatus(status);
		setMessage(message);
	}

	/**
	 * Get the command status.
	 * 
	 * @return the status
	 */
	public CommandStatus getStatus() {
		return status;
	}

	/**
	 * Set the command status, but don't allow a {@code null} status.
	 * 
	 * @param status
	 *            the status
	 */
	private void setStatus(CommandStatus status) {
		this.status = (status != null ? status : CommandStatus.OK);
	}

	/**
	 * Get the error message.
	 * 
	 * @return the error message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Set the error message, but don't allow a {@code null} message.
	 * 
	 * @param message
	 *            the error message
	 */
	private void setMessage(String message) {
		this.message = (message != null ? message : "");
	}

	/**
	 * Validate the given command and return a new {@link CommandValidator}
	 * object with {@link CommandStatus#OK} if valid, otherwise return a non-OK
	 * status plus a detailed error message.
	 * 
	 * @return return OK if valid, otherwise an error.
	 */
	public static CommandValidator validate(Command cmd) {
		if (cmd.isComment()) {
			// any comment is valid
			return new CommandValidator(CommandStatus.OK, "comment");
		}

		if (cmd.getComponentType() == null
				|| cmd.getComponentType().length() == 0) {
			// bad, no componentType
			return new CommandValidator(CommandStatus.BAD_COMPONENT_TYPE,
					"componentType is empty");
		} else if (!cmd.getComponentType().matches("\\S+")) {
			// bad, componentType contains whitespace
			return new CommandValidator(CommandStatus.BAD_COMPONENT_TYPE,
					"componentType must not contain whitespace");
		}

		if (cmd.getMonkeyId() == null || cmd.getMonkeyId().length() == 0) {
			// bad, no monkeyId
			return new CommandValidator(CommandStatus.BAD_MONKEY_ID,
					"monkeyId is empty");
		}

		if (cmd.getAction() == null || cmd.getAction().length() == 0) {
			// bar, no action
			return new CommandValidator(CommandStatus.BAD_ACTION,
					"action is empty");
		} else if (!cmd.getAction().matches("\\S+")) {
			// bad, action contains whitespace
			return new CommandValidator(CommandStatus.BAD_ACTION,
					"action must not contain whitespace");
		}
		
		//TODO: obviously lots more to validate

		return new CommandValidator(CommandStatus.OK);
	}

	@Override
	public String toString() {
		return status + (message.length() > 0 ? " : " + message : "");
	}

	/**
	 * Enum for command validation status.
	 */
	public enum CommandStatus {
		/**
		 * Bad MonkeyTalk componentType
		 */
		OK("Ok"),
		/**
		 * Bad MonkeyTalk componentType
		 */
		BAD_COMPONENT_TYPE("Bad componentType"),
		/**
		 * Bad MonkeyTalk monkeyId
		 */
		BAD_MONKEY_ID("Bad monkeyId"),
		/**
		 * Bad MonkeyTalk action
		 */
		BAD_ACTION("Bad action"),
		/**
		 * Bad MonkeyTalk command args
		 */
		BAD_ARGS("Bad args"),
		/**
		 * Bad MonkeyTalk command modifiers
		 */
		BAD_MODIFIERS("Bad modifiers");

		private String err;

		private CommandStatus(String err) {
			this.err = err;
		}

		@Override
		public String toString() {
			return this.err;
		}
	}
}