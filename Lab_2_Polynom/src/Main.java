import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {

    public static int add(int a, int b) {
        return a ^ b;
    }

    public static int multiply(int a, int b, int mod) {
        int result = 0;
        while (b != 0) {
            if ((b & 1) != 0) {
                result ^= a;
            }
            b >>= 1;
            a <<= 1;
            if ((a & (1 << (Integer.toBinaryString(mod).length() - 1))) != 0) {
                a ^= mod;
            }
        }
        return result;
    }

    public static int modInverse(int a, int mod) {
        int r0 = mod, r1 = a;
        int t0 = 0, t1 = 1;

        while (r1 != 0) {
            int q = r0 / r1;
            int temp = r0 % r1;
            r0 = r1;
            r1 = temp;

            temp = t0 ^ multiply(q, t1, mod);
            t0 = t1;
            t1 = temp;
        }

        if (r0 != 1) {
            throw new ArithmeticException("Обратный элемент не существует");
        }

        return t0;
    }

    public static int divide(int a, int b, int mod) {
        int inverse = modInverse(b, mod);
        return multiply(a, inverse, mod);
    }

    public static int power(int a, int n, int mod) {
        int result = 1;
        int base = a;

        while (n > 0) {
            if ((n & 1) != 0) {
                result = multiply(result, base, mod);
            }
            base = multiply(base, base, mod);
            n >>= 1;
        }

        return result;
    }

    public static int parsePolynomial(String polynomialString) {
        String reversed = new StringBuilder(polynomialString).reverse().toString();
        return Integer.parseInt(reversed, 2);
    }

    public static String toBinaryString(int polynomial) {
        String binary = Integer.toBinaryString(polynomial);
        return new StringBuilder(binary).reverse().toString();
    }

    public static String formatPolynomial(int poly) {
        StringBuilder result = new StringBuilder();
        boolean isFirstTerm = true;
        int degree = Integer.toBinaryString(poly).length() - 1;

        while (poly != 0) {
            if ((poly & (1 << degree)) != 0) {
                if (!isFirstTerm) {
                    result.append(" + ");
                }
                if (degree == 0) {
                    result.append("1");
                } else if (degree == 1) {
                    result.append("x");
                } else {
                    result.append("x^").append(degree);
                }
                isFirstTerm = false;
            }
            degree--;
            poly &= ~(1 << (degree + 1));
        }

        return result.length() > 0 ? result.toString() : "0";
    }

    public static void main(String[] args) throws IOException {
        String irreduciblePolynomialStr = Files.readString(Path.of("Lab_2_Polynom/src/polynom.txt")).trim();
        String inputStr = Files.readString(Path.of("Lab_2_Polynom/src/input.txt")).trim();

        int irreduciblePolynomial = parsePolynomial(irreduciblePolynomialStr);
        String[] parts = inputStr.split(" ");
        int f1 = parsePolynomial(parts[0]);
        int result = 0;

        switch (parts[1]) {
            case "+":
                int f2 = parsePolynomial(parts[2]);
                result = add(f1, f2);
                break;
            case "*":
                f2 = parsePolynomial(parts[2]);
                result = multiply(f1, f2, irreduciblePolynomial);
                break;
            case "/":
                f2 = parsePolynomial(parts[2]);
                result = divide(f1, f2, irreduciblePolynomial);
                break;
            case "^":
                int power = Integer.parseInt(parts[2]);
                result = power(f1, power, irreduciblePolynomial);
                break;
            case "-1":
                result = modInverse(f1, irreduciblePolynomial);
                break;
            default:
                throw new IllegalArgumentException("Неизвестная операция: " + parts[1]);
        }

        String resultBinary = toBinaryString(result);
        Files.writeString(Path.of("Lab_2_Polynom/src/output.txt"), resultBinary
                + '\n' + formatPolynomial(result));
    }
}
