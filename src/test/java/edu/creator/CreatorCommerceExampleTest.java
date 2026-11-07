package edu.creator;

public final class CreatorCommerceExampleTest {
    public static void main(String[] args) {
        String assignment = "{\"ok\":true,\"data\":{\"value\":\"treatment\"}}";
        String bucket = CreatorCommerceExample.stableBucket(assignment, "control");
        if (!"treatment".equals(bucket)) throw new AssertionError("expected treatment assignment");
        var release = CreatorCommerceExample.prepareRelease("algebra", "learner-7", bucket);
        if (!"course-algebra-treatment.zip".equals(release.assetName())) throw new AssertionError("asset must follow bucket");
        if (!release.subscriberMessage().contains("treatment")) throw new AssertionError("update must name bucket");
        System.out.println("CreatorCommerceExampleTest passed");
    }
}
