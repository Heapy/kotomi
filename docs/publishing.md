# Publishing Kotomi

Use `./publish-local` for development and consumer integration checks. It selects
the library modules, excludes test-only modules and build plugins, and publishes
unsigned artifacts to Maven Local.

For a Maven Central release:

1. Set the release version in `templates/publishing.module-template.yaml`.
2. Add `mavenCentral: enabled` under `settings.publishing` and set
   `signArtifacts: true`.
3. Configure `KOTLIN_TOOLCHAIN_MAVEN_CENTRAL_USERNAME`,
   `KOTLIN_TOOLCHAIN_MAVEN_CENTRAL_PASSWORD`, `KOTLIN_TOOLCHAIN_SIGNING_KEY`, and
   `KOTLIN_TOOLCHAIN_SIGNING_KEY_PASSPHRASE` in the release environment.
4. Run the build, tests, and API checks. Prepare and inspect the selected library
   bundle with `./kotlin package -m <module> --format maven-central-bundle`.
5. Publish the selected modules using
   `./kotlin publish mavenCentral -m <module> --transitive`.

Keep Maven Central's default manual release mode and inspect the deployment in
the Central Portal before releasing. Runtime and compiler-plugin versions must
stay aligned; the compiler plugin also requires the Kotlin version documented
in the runtime guide.
