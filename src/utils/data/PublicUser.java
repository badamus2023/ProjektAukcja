package utils.data;

import java.util.ArrayList;
import java.util.List;

public class PublicUser {
    private String name;
    private float valet;


    public PublicUser() {}

    public PublicUser(String name, float valet) {
        this.name = name;
        this.valet = valet;
    }

    public String getName() {
        return name;
    }

    public float getValet() {
        return valet;
    }
}