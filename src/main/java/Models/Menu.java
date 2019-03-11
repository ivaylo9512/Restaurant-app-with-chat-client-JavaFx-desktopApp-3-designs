package Models;

public class Menu {
    private int id;

    private String name;


    public Menu() {
    }

    public Menu(Menu menu){
        this.id = menu.getId();
        this.name = menu.getName();
    }

    public Menu(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
