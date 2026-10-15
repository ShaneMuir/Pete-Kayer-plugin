package com.bosshelper.loader;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;
import net.runelite.client.plugins.Plugin;

/**
 * Starts the official RuneLite client with every plugin jar found in {@code ~/.runelite/local-plugins} registered
 * first, so no developer mode and no Plugin Hub are involved. A plugin jar is any jar containing a
 * {@code runelite-plugin.properties} with a {@code plugins=} line naming its plugin classes.
 *
 * <p>Client arguments are passed straight through to {@code RuneLite.main}.
 */
public final class LocalPluginLoader
{
	private LocalPluginLoader()
	{
	}

	public static void main(String[] args) throws Exception
	{
		File dir = new File(System.getProperty("user.home"), ".runelite" + File.separator + "local-plugins");
		if (!dir.isDirectory() && !dir.mkdirs())
		{
			System.err.println("[local-plugins] could not create " + dir);
		}

		List<Class<? extends Plugin>> plugins = new ArrayList<>();
		File[] jars = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".jar"));
		if (jars != null)
		{
			Arrays.sort(jars);
			for (File jar : jars)
			{
				load(jar, plugins);
			}
		}

		if (!plugins.isEmpty())
		{
			@SuppressWarnings("unchecked")
			Class<? extends Plugin>[] array = plugins.toArray(new Class[0]);
			ExternalPluginManager.loadBuiltin(array);
		}
		System.out.println("[local-plugins] registered " + plugins.size() + " plugin(s) from " + dir);

		RuneLite.main(args);
	}

	@SuppressWarnings("unchecked")
	private static void load(File jar, List<Class<? extends Plugin>> out)
	{
		try
		{
			String names;
			try (JarFile jarFile = new JarFile(jar))
			{
				ZipEntry entry = jarFile.getEntry("runelite-plugin.properties");
				if (entry == null)
				{
					System.err.println("[local-plugins] skipping " + jar.getName() + ": no runelite-plugin.properties");
					return;
				}
				Properties props = new Properties();
				try (InputStream in = jarFile.getInputStream(entry))
				{
					props.load(in);
				}
				names = props.getProperty("plugins", "");
			}

			URLClassLoader loader = new URLClassLoader(new URL[]{jar.toURI().toURL()}, LocalPluginLoader.class.getClassLoader());
			for (String name : names.split(","))
			{
				name = name.trim();
				if (name.isEmpty())
				{
					continue;
				}
				out.add((Class<? extends Plugin>) loader.loadClass(name));
				System.out.println("[local-plugins] loaded " + name + " from " + jar.getName());
			}
		}
		catch (Throwable t)
		{
			System.err.println("[local-plugins] failed to load " + jar.getName() + ": " + t);
		}
	}
}
