package fr.joanteriihoania.peage;

public class Console {

    public static void output(String text){
        output(text, 0);
    }

    public static void output(String text, int mode){
        if (mode == 1) {
            System.err.println("[Peage] " + text);
            return;
        }

        System.out.println("[Peage] " + text);
    }

    public static void output(String[] texts){
        output(texts, 0);
    }

    public static void output(String[] texts, int mode){
        output("==========================================");
        for (String text: texts){
            output(text);
        }
        output("==========================================");
    }

}
