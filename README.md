# Bukkit Sync

This is a simple bukkit plugin that provides a service you can use to synchronize configuration between multiple servers. This works by 
synchronizing the configuration using Zookeeper and notifying the plugin of changed configurations when they happen.

It won't synchronize _all_ configuration settings, only the ones specifically configured using the plugin service.

Please note that I'm not actually using this plugin and just did it for fun. I may or may not keep working on this.

## Add as dependency

To add the library as a dependency, you can include it in your project using this repo url: `https://dl.bintray.com/kumpelblase2/Libraries`
So for gradle, this would look like this:

```groovy
repositories {
    maven { url 'https://dl.bintray.com/kumpelblase2/Libraries' }
}
```

And use the library like this:
```groovy
dependencies {
    compile 'de.eternalwings.bukkit:sync-api:1.0.0'
}
```

## Setup

Since this plugin uses Zookeeper to work, it needs to be running somewhere. By default the plugin will try to connect to zookeeper on 
`localhost:2181`. If zookeeper is not running on the same host, create a `config.yml` file in a folder called `Sync` inside the plugins directory 
with the following content:
```yaml
zookeeper: <zookeeperip>:<zookeeperport>
```

and replace the placeholders with the appropriate values.

When you start the server now, the plugin should already be able to connect to zookeeper and announcing that in the log as well.

Please note that this requires zookeeper version 3.5 or above.

## Plugin usage

To actually take care of this plugin, you would want to tell it which configuration keys to synchronize. To do that, get the service from bukkit, 
create a synchronized version of your configuration and tell it to synchronize a key:

```java
class MyPlugin extends JavaPlugin {
    public void onEnable() {
        SyncService sync = this.getServer().getServicesManager().getRegistration(SyncService.class).getProvider();
        SynchronizedConfig synchronizedConfig = sync.getSynchronizedConfig(this.getConfig(), this);
        synchronizedConfig.synchronizeKey("key1", "defaultValue", String.class, (oldValue, newValue) -> {
            System.out.println("Value changed from " + oldValue + " to " + newValue);
        });
    }
}
```

Given that you're using a service that this plugin provides, make sure that you also depend on this plugin in your `plugin.yml`.

A `SynchronizedConfig` is just like a normal configuration, so you can use `get` and `set` like you usually would, with the difference being that 
the synchronized key values will be taken from zookeeper instead of from the local configuration.

Now upon server start it will save the default value to zookeeper (if it doesn't exist) and notify you about the value change. Now once the value 
changes in zookeeper, for whatever reason, you will be notified and it will automatically be persisted to your local config.

## Instance Discovery

You can also use this to keep track of running instances. For this you need to add additional information to the plugin 
configuration:

```yaml
announce_instance: true
instance: Uber
host: localhost:25565
```

Where `instance` is a globally unique name of the server instance and `host` is the host (+port) under which other instances 
could access it. That may be an IP or a hostname, depending on what's possible. Now upon starting the server, the plugin will 
register itself as an instance so other instances can see it. Instances will be visible in zookeeper under 
`/minecraft/instances/<instance_name>`. Inside your plugin, you can listen on instance changes (new instance, instance lost, 
instance changed) like this:

```java
class MyPlugin extends JavaPlugin {
    public void onEnable() {
        SyncService sync = this.getServer().getServicesManager().getRegistration(SyncService.class).getProvider();
        InstanceWatcher watcher = sync.getInstanceWatcher();
        watcher.onInstanceFound(instance -> {
            this.getLogger().info("Found Instance:" + instance.getName());
        });
        
        watcher.getAllInstances().forEach(instance -> {
            this.getLogger().info("Instance Available: " + instance.getName());
        });
    }
}
```

Each instance will provide you with its name, host(+port) and the list of plugins configured on that instance.

## Building

Just run `gradle shadowJar` and copy the jar from `build/libs` into the plugins dir of your server.
