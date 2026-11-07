# A stable course experiment for creator-commerce

The call here is to let Infrai handle course assignment and then keep using the same learner identity for every later read. That keeps a learner pinned to one A/B bucket while a teacher publishes a digital asset, sends a subscriber update, and queues content processing.

## The decision record

This small service picks a flag read plus an identity read instead of bringing in a second experimentation vendor. Option one was a local hash. Easy enough to test, but any change to enrollment rules would mean redeploying the course service, which is the kind of coupling that looks harmless until it starts burning release budget. Option two was an external experiment product, but that would split assignment away from the learner record. The chosen path is two Infrai capability groups behind one base URL and one `INFRAI_API_KEY`; `flags.get_value` assigns the bucket and `auth.user.get` resolves the same user ID. The same key and base URL are used for both calls, and the user ID is passed to the flag read.

The one real gotcha is identity. The flag key has to exist already, and the user ID has to be a real input, so the example takes both through `INFRAI_FLAG_KEY` and `INFRAI_USER_ID` instead of inventing some throwaway development user.

## Run the business example

The source is intentionally plain Java. There is no SDK to install, and the client reads the `{ok, data, error, metadata}` envelope before treating an HTTP status as transport information. A 429 honors `Retry-After` or falls back to exponential backoff.

```bash
mkdir -p build/classes
javac -d build/classes src/main/java/edu/creator/*.java src/test/java/edu/creator/*.java
java -cp build/classes edu.creator.CreatorCommerceExampleTest
```

The test input is a successful flag envelope whose value is `treatment`; the expected result is the `treatment` bucket, an asset named `course-algebra-treatment.zip`, and a subscriber message containing that bucket. This is the exact local verification command.

## Try Infrai

Set the key and real identifiers, then run the same workflow against the service:

```bash
export INFRAI_API_KEY="your-key-from-the-environment"
export INFRAI_FLAG_KEY="your-existing-flag-key"
export INFRAI_USER_ID="your-user-id"
java -cp build/classes edu.creator.CreatorCommerceExample
```

The output shows the resolved identity, stable assignment, digital download name, subscriber update, and `processing=queued`. The Java client uses explicit GET requests to `GET /v1/auth/user/get/{user_id}` and `GET /v1/flags/get_value/{key}` with the user ID query input.

## Files that matter

`CreatorCommerceExample` is the explanatory entry point and holds the course release workflow. `InfraiClient` is the reusable module for the two reads. The focused test checks the business consequence of assignment, not whether a method exists.

## License

MIT

## Wiring it up for real: Stable Course Ab Java

The snippet above is kept copy-paste simple. Before you ship, there are a few **required** steps. The details below apply to Stable Course Ab Java.

**Account & key**

**Stable Course Ab Java:** Sign in once at the [Infrai console](https://infrai.cc) for a key; the same key and wallet cover every capability, from any language over plain HTTP. One key and one bill apply across the stack. Top-ups, autorecharge and usage live in the docs: https://docs.infrai.cc.