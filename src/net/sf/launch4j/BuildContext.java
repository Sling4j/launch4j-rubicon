package net.sf.launch4j;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import net.sf.launch4j.config.Config;

public class BuildContext implements AutoCloseable {

	private static ThreadLocal<BuildContext> contexts = new ThreadLocal<>();

	@Getter
	@Setter
	private Config config;

	private Map<String, String> vars = new HashMap<>();

	protected BuildContext() {
	}

	public static BuildContext get(Config config) {
		BuildContext bc = current();
		if (contexts.get() != null) {
			return bc;
		}

		bc = new BuildContext();
		bc.setConfig(config);
		contexts.set(bc);
		return bc;
	}

	public static BuildContext current() {
		return contexts.get();
	}

	@Override
	public void close() throws Exception {
		contexts.remove();
	}

	public BuildContext(Config config) {
		this.config = config;
	}

	/**
	 * 
	 * @param delete
	 *            if true don't pass the variable to the resulting exe.
	 */
	public String getVariable(String varName, boolean delete, String defValue) {
		if (vars.containsKey(varName)) {
			return vars.get(varName);
		}

		String prefix = varName + "=";
		String varValue = defValue;
		String foundLine = null;
		for (String s : config.getVariables()) {
			if (s.startsWith(prefix)) {
				foundLine = s;
				varValue = s.substring(prefix.length());
			}
		}
		if (delete && foundLine != null) {
			config.getVariables().remove(foundLine);
		}

		if (varValue != null) {
			vars.put(varName, varValue);
		}

		return varValue;
	}

	/**
	 * 
	 * @param persistent
	 *            if true pass the variable to resulting exe.
	 */
	public void setVariable(String varName, String varValue, boolean persistent) {
		vars.put(varName, varValue);

		if (persistent) {
			String varPrefix = varName + "=";
			config.getVariables().removeIf((t) -> {
				return t.startsWith(varPrefix);
			});

			String varLine = varPrefix + varValue;
			config.getVariables().add(varLine);
		}

	}
}
