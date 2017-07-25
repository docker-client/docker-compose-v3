# docker-compose-v3

### About

This is a small library to load Docker Compose v3 YAML files for further consumption. The driving force is the [Docker Client](https://github.com/gesellix/docker-client) library, where the equivalent to `docker stack deploy` needs to load such Compose v3 YAML files.

A bit more clarification can be found at [issue #2](https://github.com/docker-client/docker-compose-v3/issues/2).

### Usage

Given a Compose file being available as `InputStream` and its `workingDir` you can read the YAML file like this:

    InputStream composeStack = getClass().getResourceAsStream('docker-stack.yml')
    String workingDir = Paths.get(getClass().getResource('docker-stack.yml').toURI()).parent
    ComposeConfig composeConfig = new ComposeFileReader().load(composeStack, workingDir, System.getenv())
    ...

You can find a real life example at the [Docker Client's DeployConfigReader](https://github.com/gesellix/docker-client/blob/0ee342ef0d766c44909f83cb6fba720ed627fcc5/client/src/main/groovy/de/gesellix/docker/client/stack/DeployConfigReader.groovy#L55).

### Contributing

Feel free to submit an issue if you'd like to ask a question or propose a new feature (maybe Compose v2 support?). Pull Requests are welcome, too!
