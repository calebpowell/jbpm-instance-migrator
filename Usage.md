# Introduction #

The following page explains how to use the jbpm-instance-migrator library.

**Defining a Migrator**

A Migrator is responsible for loading and applying 1 or more migrations on a ProcessInstance. A Migrator is created as follows:

```
Migrator migrator = new Migrator(“ApplicationProcess”, jbpmContext, “com.foobar.ApplicationProcessMigration”);
```

The parameters used to create the Migrator instance are:

  1. The name of the Process Definition that it will be migrating.
  1. A JbpmContext instance. The migrator requires this to look up the latest Process Definition for the ProcessInstance.
  1. The Migration base class name. The migrator assumes that your migrations use the pattern _package.ClassName{migration#}_. For example, if your base Class name _“com.foobar.ApplicationProcessMigration”_, the migrator will attempt to load and instantiate classes named “com.foobar.ApplicationProcessMigration001”, “com.foobar.ApplicationProcessMigration002”, etc, until it can’t find any valid classes in the Classpath.


Once you have instantiated a migrator, you invoke the _migrate()_ method and pass it an instance of a ProcessInstance:

```
ProcessInstance newProcessInstance = migrator.migrate(oldProcessInstance);
```

The _migrate()_ method will create and return a new ProcessInstance based on the latest ProcessDefinition. The new ProcessInstance will contain a new ContextInstance and tokens in the appropriate state nodes.

**Defining a Migration**

Migrations are written as Java classes, loaded and then applied by a Migrator. The class must implement the org.jbpm.instance.migration.Migration interface, it must not be abstract, and it must contain a default constructor. The Migration interface declares one method called 'createNodeMap()', whose return value is an instance of a StateNodeMap. A StateNodeMap is used by a migrator to determine which nodes have been deprecated and which nodes have superseded the deprecated nodes. In the following ApplicationProcessMigration001 migration, the init node has been deprecate and superseded by the start node:

```
public class ApplicationProcessMigration001 implements Migration{
   
   public StateNodeMap createNodeMap() {
       return new StateNodeMap(new String[][]{
               {"init", "start"}       
       });
   }
}

```

So, if a migrator is passed a ProcessInstance that has a token in an 'init' state node, the migrator will create new ProcessInstance (based on the latest ProcessDefinition), and create a token in it's 'start' state node.

**Extensions**

If order to address any custom requirement you may have, the migrator allows you to add 1 or more MigrationHandler implementations. The MigrationHandler interface looks like this:

```
public interface MigrationHandler {

    public void migrateInstance(ProcessInstance oldProcessInstance, ProcessInstance newProcessInstance);
   
}
```

You can add your custom implementations to the migrator instance like so:

```
MigrationHandler customMigrationHandler = new MyCustomMigrationHandler();
migrator.addMigrationHandler(customMigrationHandler);
```

The MigrationHandler instances are stored in a list and will be invoked in the order that you add them.