# Scroll
### Skript language for Fabric API

Under development.

Documentation will be coming when in a releasable state.

Mention me `limeglass` on SkUnity Discord for any information.

---

## Contributing

Pull requests always open.

skript-parser uses GitHub packages, and is required by Scroll.

To download GitHub packages you'll have to include your GitHub username and a GitHub token.

See https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens for how to obtain a token (you'll only need permission read:packages).

Add this to a gradle.properties at your gradle global cache (Windows users: C:/Users/USER/.gradle/gradle.properties)
```
scrollUsername=USERNAME
scrollPassword=githubToken
```

### Setup
```
git clone https://github.com/ScrollLang/Scroll
cd /Scroll
gradlew clean genSources
gradlew build
```
You must generate the Minecraft sources with the genSources command to be able to use Minecraft methods in your IDE

### Running the Client or Server
```
gradlew runClient
gradlew runServer
```

### Code Conventions
Scroll follows Skript's code conventions excluding the section about License

https://github.com/SkriptLang/Skript/blob/master/code-conventions.md
