package uit.linh.providers;

import java.util.ArrayList;

/**
 *
 * Created by linh on 09/06/2015.
 */
public class Colors {

    private ArrayList<Color> colors;
    public static final String[] stringColors = {
            "#ecf0f1", "#95a5a5", "#3d556d", "#9a59b5", "#3598db",
            "#b3c3c7", "#7e8c8d", "#2d3e50", "#8d44ad", "#297fb8",
            "#f1c40f", "#e67e22", "#e84c3d", "#1bbc9b", "#12dcc7",
            "#f39c11", "#d25400", "#c1293b", "#16a086", "#27ae61"
    };

    public Colors() {
        colors = new ArrayList<>();
        for (int i=0; i< 20; i++){
            Color color = new Color(stringColors[i], false);
            colors.add(color);
        }
    }

    public ArrayList<Color> getColors() {
        return colors;
    }

    public Color getColor(int i){
        return colors.get(i);
    }

    public class Color{
        private String color;
        private boolean checked;

        public void setChecked(boolean checked) {
            this.checked = checked;
        }

        public String getColor() {
            return color;
        }

        public boolean isChecked() {
            return checked;
        }

        private Color(String color, boolean checked) {
            this.color = color;
            this.checked = checked;
        }
    }
}
