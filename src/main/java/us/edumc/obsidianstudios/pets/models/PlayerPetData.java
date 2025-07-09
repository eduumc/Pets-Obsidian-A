package us.edumc.obsidianstudios.pets.models;

public class PlayerPetData {

    private final String petId;
    private String customName;
    private String particleType;
    private FollowStyle followStyle;
    private boolean particlesEnabled;
    private boolean displayNameVisible;
    private int level;
    private double xp;

    public PlayerPetData(String petId) {
        this.petId = petId;
        this.customName = null;
        this.particleType = null;
        this.followStyle = FollowStyle.SIDE_RIGHT;
        this.particlesEnabled = true;
        this.displayNameVisible = true;
        this.level = 1;
        this.xp = 0;
    }

    // Getters y Setters
    public String getPetId() { return petId; }
    public String getCustomName() { return customName; }
    public void setCustomName(String customName) { this.customName = customName; }
    public String getParticleType() { return particleType; }
    public void setParticleType(String particleType) { this.particleType = particleType; }
    public FollowStyle getFollowStyle() { return followStyle; }
    public void setFollowStyle(FollowStyle followStyle) { this.followStyle = followStyle; }
    public boolean isParticlesEnabled() { return particlesEnabled; }
    public void setParticlesEnabled(boolean particlesEnabled) { this.particlesEnabled = particlesEnabled; }
    public boolean isDisplayNameVisible() { return displayNameVisible; }
    public void setDisplayNameVisible(boolean displayNameVisible) { this.displayNameVisible = displayNameVisible; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public double getXp() { return xp; }
    public void setXp(double xp) { this.xp = xp; }
    public void addXp(double amount) { this.xp += amount; }
}