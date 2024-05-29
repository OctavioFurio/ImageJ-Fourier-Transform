public class ComplexNumber {
    double real, image;
 
    public ComplexNumber(double r, double i)
    {
        this.real = r;
        this.image = i;
    }

    public static ComplexNumber add(ComplexNumber n1, ComplexNumber n2)
    {
        ComplexNumber res = new ComplexNumber(0, 0);
        res.real = n1.real + n2.real;
        res.image = n1.image + n2.image;
        return res;
    }
}