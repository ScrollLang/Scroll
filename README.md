# Scroll
### Skript language for Fabric API

Under development.

Documentation will be coming when in a releasable state.

Mention me `limeglass` on SkUnity Discord for any information.

### Requirements
- Fabric API (Currently 0.88 but not locked in this version)
- Minecraft (Currently 1.20.1 but not locked and only supporting this version currently)
- Java 17+

---

## Contributing

Pull requests open.

skript-parser uses GitHub packages, and is required by Scroll.

To download GitHub packages you'll have to include your GitHub username and a GitHub token.

See https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens for how to obtain a token (you'll only need permission read:packages).

Add this to a gradle.properties at your gradle global cache C:/Users/USER/.gradle/gradle.properties or in the gradle.properties of the project. Preferably the first latter to avoid pushing tokens.
```
scrollUsername=USERNAME
scrollPassword=githubToken
```

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
