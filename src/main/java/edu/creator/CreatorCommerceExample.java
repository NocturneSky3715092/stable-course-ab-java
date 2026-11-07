package edu.creator;

import java.io.IOException;
import java.util.Locale;

/** Runnable course-publishing example: assignment, delivery, updates, processing. */
public final class CreatorCommerceExample {
    public record CourseRelease(String courseId, String creatorId, String assetName, String subscriberMessage) {}

    public static String stableBucket(String flagEnvelope, String fallback) {
        String lower = flagEnvelope.toLowerCase(Locale.ROOT);
        if (lower.contains("control")) return "control";
        if (lower.contains("treatment")) return "treatment";
        return fallback;
    }

    public static CourseRelease prepareRelease(String courseId, String creatorId, String bucket) {
        String asset = "course-" + courseId + "-" + bucket + ".zip";
        String message = "New lesson materials are ready for " + creatorId + " (" + bucket + ").";
        return new CourseRelease(courseId, creatorId, asset, message);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        String key = System.getenv("INFRAI_API_KEY");
        String flagKey = args.length > 0 ? args[0] : System.getenv("INFRAI_FLAG_KEY");
        String userId = args.length > 1 ? args[1] : System.getenv("INFRAI_USER_ID");
        if (key == null || flagKey == null || userId == null) {
            throw new IllegalArgumentException("Set INFRAI_API_KEY, INFRAI_FLAG_KEY, and INFRAI_USER_ID.");
        }
        InfraiClient client = new InfraiClient(key);
        String identity = client.user(userId);
        String assignment = client.flagValue(flagKey, userId);
        String bucket = stableBucket(assignment, "control");
        CourseRelease release = prepareRelease("java-basics", userId, bucket);
        System.out.println("identity=" + identity);
        System.out.println("bucket=" + bucket);
        System.out.println("download=" + release.assetName());
        System.out.println("subscriber_update=" + release.subscriberMessage());
        System.out.println("processing=queued");
    }
}
