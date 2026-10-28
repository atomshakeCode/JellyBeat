# CI build and Modrinth review

This repository contains the JellyBeat 0.7.1 source and a GitHub Actions workflow at `.github/workflows/build.yml`.

## Run the automated build

1. Upload/commit all source files, including the hidden `.github/workflows/build.yml`, to the repository's default branch (`main` or `master`).
2. Open the GitHub repository's **Actions** tab and select **Build JellyBeat 0.7.1**. A push touching the source also starts the workflow automatically.
3. Wait for every job step to finish green. If it fails, do not submit the previous local JAR; open the failed step's log first.
4. Open that successful run, download `JellyBeat-0.7.1-verified-build`, and extract it. It contains the CI-built mod JAR, the rebuilt WASAPI helper, and SHA-256 hashes.
5. Upload the JAR from that successful run to the Modrinth version, replacing the prior locally built JAR. Keep the workflow run URL and repository link for moderator review.
6. Only after the CI-built JAR is the uploaded Modrinth file, resubmit the project for review.

The helper is compiled on the pinned Windows runner with the Windows .NET Framework compiler from `native/Capture.cs` via `native/build.ps1`. Java sources are compiled by `build.py`. CI downloads pinned compile-only versions of Minecraft/Fabric, Fabric API, and Simple Voice Chat. Simple Voice Chat classes are not bundled in JellyBeat.

The SHA-256 file identifies the outputs of that exact run. Because the legacy .NET Framework compiler can include per-build data in PE files, compare the JAR and hashes from the same successful run; do not assume separate builds will have identical hashes.
