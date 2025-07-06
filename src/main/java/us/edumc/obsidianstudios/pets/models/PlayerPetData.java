package us.edumc.obsidianstudios.pets.models;

public class PlayerPetData {

    private final String petId;
    private String customName;
    private String particleType;
    private FollowStyle followStyle;
    private boolean particlesEnabled;

    public PlayerPetData(String petId) {
        this.petId = petId;
        this.customName = null;
        this.particleType = null;
        this.followStyle = FollowStyle.SIDE_RIGHT;
        this.particlesEnabled = true;
    }

    public String getPetId() { return petId; }
    public String getCustomName() { return customName; }
    public void setCustomName(String customName) { this.customName = customName; }
    public String getParticleType() { return particleType; }
    public void setParticleType(String particleType) { this.particleType = particleType; }
    public FollowStyle getFollowStyle() { return followStyle; }
    public void setFollowStyle(FollowStyle followStyle) { this.followStyle = followStyle; }
    public boolean isParticlesEnabled() { return particlesEnabled; }
    public void setParticlesEnabled(boolean particlesEnabled) { this.particlesEnabled = particlesEnabled; }
}