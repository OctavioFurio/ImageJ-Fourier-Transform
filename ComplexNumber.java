public class ComplexNumber 
{
    double real, image, modulo;
 
    public ComplexNumber(double real, double imaginary)
    {
        this.real = real;
        image     = imaginary;
        modulo    = Math.sqrt((real * real) + (image * image));
    }

    public ComplexNumber add(ComplexNumber other)
    {
        return new ComplexNumber(
            this.real  + other.real, 
            this.image + other.image
        );
    }

    public ComplexNumber multiply(ComplexNumber other) 
    {
        double newReal = this.real * other.real - this.image * other.image;
        double newImaginary = this.real * other.image + this.image * other.real;
        return new ComplexNumber(newReal, newImaginary);
    }

    public ComplexNumber multiply(double other) 
    {
        return new ComplexNumber(
            this.real * other, 
            this.image * other
        );
    }

    @Override
    public String toString() 
    {
        return this.real + " + " + this.image + "i";
    }
}