package us.edumc.obsidianstudios.pets.models;

public enum FollowStyle {
    SIDE_RIGHT("Al Lado Derecho"),
    SIDE_LEFT("Al Lado Izquierdo"),
    BEHIND("Detrás"),
    ABOVE("Arriba de la Cabeza");

    private final String displayName;
    FollowStyle(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }
}
