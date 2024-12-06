import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {

    public static long add(long a, long b) {
        return a ^ b;
    }

    public static long getPolynomDegree(long a) {
        long degree = -1;
        while (a != 0) {
            a >>= 1;
            degree++;
        }
        return degree;
    }

    public static long multiply(long a, long b, long mod) {
        long modDegree = getPolynomDegree(mod);
        long result = 0;
        while (a != 0) {
            if ((a & 1) == 1)
                result ^= b;
            a >>= 1;
            b <<= 1;
            if ((b & (1L << modDegree)) != 0)
                b ^= mod;
        }

        return result;
    }
    public static long inverse(long a, long mod) {
        long r0 = mod;
        long r1 = a;
        long t = 0, s = 1;
        long[] quotient = new long[1];
        while (r1 != 0) {
            long buf = divide(r0, r1, quotient);
            r0 = r1;
            r1 = buf;
            buf = t ^ multiply(quotient[0], s, mod);
            t = s;
            s = buf;
        }

        return t;
    }

    public static long divide (long a, long b, long[] quot) {
        long aDegree = getPolynomDegree(a);
        long bDegree = getPolynomDegree(b);
        quot[0] = 0;
        while (aDegree >= bDegree) {
            quot[0] ^= (1L << (aDegree - bDegree));
            a ^= b << (aDegree - bDegree);
            aDegree = getPolynomDegree(a);
        }
        return a;
    }

    public static long mod(long a, long mod) {
        long polyDegree = getPolynomDegree(a);
        long modDegree = getPolynomDegree(mod);

        while (polyDegree >= modDegree) {
            a ^= mod << (polyDegree - modDegree);
            polyDegree = getPolynomDegree(a);
        }

        return a;
    }


    public static long power(long a, long n, long mod) {
        long result = 1;
        long base = a;

        while (n > 0) {
            if ((n & 1) != 0) {
                result = multiply(result, base, mod);
            }
            base = multiply(base, base, mod);
            n >>= 1;
        }

        return result;
    }

    public static long parsePolynomial(String polynomialString) {
        String reversed = new StringBuilder(polynomialString).reverse().toString();
        return Long.parseLong(reversed, 2);
    }

    public static String toBinaryString(long polynomial) {
        String binary = Long.toBinaryString(polynomial);
        return new StringBuilder(binary).reverse().toString();
    }

    public static String formatPolynomial(long poly) {
        StringBuilder result = new StringBuilder();
        boolean isFirstTerm = true;
        long degree = Long.toBinaryString(poly).length() - 1;

        while (poly != 0) {
            if ((poly & (1L << degree)) != 0) {
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
            poly &= ~(1L << (degree + 1));
        }

        return !result.isEmpty() ? result.toString() : "0";
    }

    public static void main(String[] args) throws IOException {
        String irreduciblePolynomialStr = Files.readString(Path.of("Lab_2_Polynom/src/polynom.txt")).trim();
        String inputStr = Files.readString(Path.of("Lab_2_Polynom/src/input.txt")).trim();

        long irreduciblePolynomial = parsePolynomial(irreduciblePolynomialStr);
        String[] parts = inputStr.split(" ");
        long f1 = parsePolynomial(parts[0]);
        f1 = mod(f1, irreduciblePolynomial);
        long result = 0;

        switch (parts[1]) {
            case "+":
                long f2 = parsePolynomial(parts[2]);
                f2 = mod(f2, irreduciblePolynomial);
                result = add(f1, f2);
                break;
            case "*":
                f2 = parsePolynomial(parts[2]);
                f2 = mod(f2, irreduciblePolynomial);
                result = multiply(f1, f2, irreduciblePolynomial);
                break;
            case "/":
                f2 = parsePolynomial(parts[2]);
                f2 = mod(f2, irreduciblePolynomial);
                result = divide(f1, f2, new long[]{irreduciblePolynomial});
                break;
            case "^":
                long power = Long.parseLong(parts[2]);
                result = power(f1, power, irreduciblePolynomial);
                break;
            case "^-1":
                result = inverse(f1, irreduciblePolynomial);
                break;
            default:
                throw new IllegalArgumentException("Неизвестная операция: " + parts[1]);
        }

        String resultBinary = toBinaryString(result);
        Files.writeString(Path.of("Lab_2_Polynom/src/output.txt"), resultBinary
                + '\n' + formatPolynomial(result));
    }

}
