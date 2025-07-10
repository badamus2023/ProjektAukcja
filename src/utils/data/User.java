package utils.data;

public class User {
    private String name;
    private String password;
    private float valet;

    public User() {}

    public User(String name, String password) {
        this.name = name;
        this.password = password;
        this.valet = 0f;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public float getValet() {
        return valet;
    }

    public void setValet(float valet) {
        this.valet = valet;
    }


    @Override
    public String toString() {
        return "User{name='" + name + "', valet=" + valet  + " password= " + password + "}";
    }
}
