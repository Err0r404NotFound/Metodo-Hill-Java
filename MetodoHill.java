import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class MetodoHill {
    
    // Diccionario para encriptar: convierte letras a números
    static Map<Character, Integer> diccionarioEncryt = new HashMap<>();
    static Map<Integer, Character> diccionarioDecrypt = new HashMap<>();
    static int moduloUtilizado = 0;
    
    static {
        //Diccionario utilizado cambiar a gusto, en este caso se utiliza el del Escenario planteado mod 28
        String alfabeto = "A B C D E F G H I J K L M N Ñ O P Q R S T U V W X Y Z _";
        String[] letras = alfabeto.split(" ");
        for (int i = 0; i < letras.length; i++) {
            diccionarioEncryt.put(letras[i].charAt(0), i);
            diccionarioDecrypt.put(i, letras[i].charAt(0));
        }
        moduloUtilizado = diccionarioEncryt.size();
    }

    // Matriz llave fija (Cambiar a gusto) (Lo mismo en este caso se utiliza la del escenario)
    static int[][] key = { {4, 3, 1}, {2, 2, 1}, {1, 1, 1} };

    // Calcula inverso modular usando algoritmo extendido de Euclides
    public static int modInv(int a, int m) {
        int m0 = m, x0 = 0, x1 = 1;

        if (m == 1) return 0;
        
        while (a > 1) {
            int q = a / m;
            int temp = m;
            m = a % m;
            a = temp;

            temp = x0;
            x0 = x1 - q * x0;
            x1 = temp;
        }

        if (x1 < 0) x1 += m0; // aseguramos el valor positivo del inverso

        return x1;
    }
    // Inversa de matriz módulo n
    public static int[][] invertMatrixMod(int[][] matrix, int mod) throws Exception {
        int det = determinant(matrix);
        int detInv = modInv(det, mod);

        if (det == 0) throw new Exception("La matriz no es invertible.");

        int[][] adjugate = adjugate(matrix);
        int[][] invMatrix = new int[matrix.length][matrix.length];
        
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                invMatrix[i][j] = (adjugate[i][j] * detInv) % mod;
                if (invMatrix[i][j] < 0) invMatrix[i][j] += mod;
            }
        }
        return invMatrix;
    }
    // Determinante de la matriz 3x3 en este caso { {4, 3, 1}, {2, 2, 1}, {1, 1, 1} }
    public static int determinant(int[][] matrix) {
        return matrix[0][0] * (matrix[1][1] * matrix[2][2] - matrix[1][2] * matrix[2][1])
             - matrix[0][1] * (matrix[1][0] * matrix[2][2] - matrix[1][2] * matrix[2][0])
             + matrix[0][2] * (matrix[1][0] * matrix[2][1] - matrix[1][1] * matrix[2][0]);
    }
    // Matriz adjunta (cofactores)
    //Se usa la matriz adjunta junto con el determinante de la matriz original para calcular su inversa.
    public static int[][] adjugate(int[][] matrix) {
        int[][] adj = new int[3][3];
        adj[0][0] = matrix[1][1] * matrix[2][2] - matrix[1][2] * matrix[2][1];
        adj[0][1] = -(matrix[0][1] * matrix[2][2] - matrix[0][2] * matrix[2][1]);
        adj[0][2] = matrix[0][1] * matrix[1][2] - matrix[0][2] * matrix[1][1];
        adj[1][0] = -(matrix[1][0] * matrix[2][2] - matrix[1][2] * matrix[2][0]);
        adj[1][1] = matrix[0][0] * matrix[2][2] - matrix[0][2] * matrix[2][0];
        adj[1][2] = -(matrix[0][0] * matrix[1][2] - matrix[0][2] * matrix[1][0]);
        adj[2][0] = matrix[1][0] * matrix[2][1] - matrix[1][1] * matrix[2][0];
        adj[2][1] = -(matrix[0][0] * matrix[2][1] - matrix[0][1] * matrix[2][0]);
        adj[2][2] = matrix[0][0] * matrix[1][1] - matrix[0][1] * matrix[1][0];
        return adj;
    }
    // Encripta el mensaje usando la matriz llave
    public static String encriptar(String message, int[][] key) {
        message = message.toUpperCase();
        int size = key.length;
        StringBuilder ciphertext = new StringBuilder();

        while (message.length() % size != 0) {
            message += "X";
        }

        for (int i = 0; i < message.length(); i += size) {
            int[] block = new int[size];
            for (int j = 0; j < size; j++) {
                block[j] = diccionarioEncryt.get(message.charAt(i + j));
            }

            for (int j = 0; j < size; j++) {
                int val = 0;
                for (int k = 0; k < size; k++) {
                    val += key[j][k] * block[k];
                }
                val %= moduloUtilizado;
                ciphertext.append(diccionarioDecrypt.get(val));
            }
        }
        return ciphertext.toString();
    }
    // Desencripta el mensaje usando la matriz inversa
    public static String desencriptar(String message, int[][] key) throws Exception {
        int size = key.length;
        StringBuilder plaintext = new StringBuilder();
        int[][] keyInv = invertMatrixMod(key, moduloUtilizado);

        for (int i = 0; i < message.length(); i += size) {
            int[] block = new int[size];
            for (int j = 0; j < size; j++) {
                block[j] = diccionarioEncryt.get(message.charAt(i + j));
            }

            for (int j = 0; j < size; j++) {
                int val = 0;
                for (int k = 0; k < size; k++) {
                    val += keyInv[j][k] * block[k];
                }
                val %= moduloUtilizado;
                if (val < 0) val += moduloUtilizado;
                plaintext.append(diccionarioDecrypt.get(val));
            }
        }
        return plaintext.toString().replaceAll("X+$", ""); // Eliminamos 'X' de relleno
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("Escriba el mensaje a Encriptar:");
        String message = sc.nextLine();

        System.out.println("Mensaje original: " + message);
        String encrypted = encriptar(message, key);
        System.out.println("Mensaje encriptado: " + encrypted);
        try {
            String decrypted = desencriptar(encrypted, key);
            System.out.println("Mensaje desencriptado: " + decrypted);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        System.out.println("Diccionario Utilizado:");
        System.out.println(diccionarioEncryt);
        System.out.println("Módulo Utilizado: " + moduloUtilizado);

        sc.close();
    }
}
