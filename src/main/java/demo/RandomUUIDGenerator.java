package demo;

import java.util.UUID;

// cia pageneruosim random UUID
public class RandomUUIDGenerator implements UuidGenerator {
    @Override
    public UUID generator() {
        return UUID.randomUUID();
    }
}
