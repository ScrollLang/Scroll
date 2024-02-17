# Scroll
### Skript language for Fabric API

Under development.

Documentation will be coming when in a releasable state.

Mention me `limeglass` on SkUnity Discord for any information.

### Requirements
- Fabric API (Currently 0.88 but not locked in this version)
- Minecraft (Currently 1.20.1 but not locked and only supporting this version and latest currently)
- Java 17+

---

## Contributing

Pull requests open.

### Setup
```
git clone https://github.com/TheLimeGlass/Scroll
cd /Scroll
git submodule update --init --recursive
gradlew clean genSources
gradlew build
```
You must generate the Minecraft sources with the genSources command to be able to use Minecraft methods in your IDE

### Running the Client or Server
```
gradlew runClient
gradlew runServer
```

### Updating your local branch of Scroll with the latest skript-parser commit
```
git submodule update --recursive --remote
```
This is assuming you executed `git submodule update --init --recursive` before, as this must be done before running this command.

### Code Conventions
Scroll follows Skript's code conventions excluding the section about License

https://github.com/SkriptLang/Skript/blob/master/code-conventions.md
