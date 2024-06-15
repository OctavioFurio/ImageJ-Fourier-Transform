public class ComplexNumber 
{
    double real, image;
 
    public ComplexNumber(double real, double imaginary)
    {
        this.real = real;
        this.image = imaginary;
    }

    public double modulo()
    {
        return Math.sqrt((real * real) + (image * image));
    }

    public void add(ComplexNumber other)
    {
        real += other.real;
        image += other.image;
    }

    public static ComplexNumber multiply(ComplexNumber first, ComplexNumber second) 
    {
        double newReal = first.real * second.real - first.image * second.image;
        double newImaginary = first.real * second.image + first.image * second.real;

        return new ComplexNumber(newReal, newImaginary);
    }

    public void multiply(ComplexNumber other) 
    {
        double newReal = this.real * other.real - this.image * other.image;
        double newImaginary = this.real * other.image + this.image * other.real;

        real = newReal;
        image = newImaginary;
    }

    public void multiply(double other) 
    {
        real *= other;
        image *= other;
    }

    @Override
    public String toString() 
    {
        return this.real + " + " + this.image + "i";
    }
}