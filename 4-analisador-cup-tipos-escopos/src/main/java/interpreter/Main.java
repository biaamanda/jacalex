package interpreter;

public class Main {

    public static void main(String[] args) {
        java.io.Reader reader = null;
        if (args.length == 0) {
            reader = new java.io.InputStreamReader(System.in, java.nio.charset.StandardCharsets.UTF_8);
        } else {
            if (args.length != 1) {
                System.out.println("Uso : java -jar tiposescopos <nome do arquivo>");
            } else {
                try {
                    java.io.FileInputStream stream = new java.io.FileInputStream(args[0]);
                    reader = new java.io.InputStreamReader(stream, java.nio.charset.StandardCharsets.UTF_8);
                } catch (java.io.FileNotFoundException e) {
                    System.out.println("Arquivo nao encontrado : \"" + args[0] + "\"");
                } catch (Exception e) {
                    System.out.println("Exceçao de arquivo: " + e);
                }
            }
        }
        if (reader == null) {
            return;
        }
        try {
            Scanner scanner = new Scanner(reader);
            Parser parser = new Parser(scanner);
            parser.parse();
        } catch (Exception e) {
            System.out.println("Exceçao: " + e.getMessage());
        }
    }
}
