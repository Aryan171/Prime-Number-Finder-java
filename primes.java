import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class App extends Thread{
    static long[] a, b; // used for get and set bits in a long
    static long product = 1l, // to store product of first sf prime numbers
    ncp = 0l, // number of primes in the sieve
    primelist[] = {2l, 3l, 5l, 7l, 11l, 13l, 17l, 19l, 23l},
    redSieve[]; // reduced sieve, it is a list of all the locations where primes can be

    static String outStr = "", prntStr = ""; 
    static volatile boolean prnt = false; // prnt is turned true when the main thread wants to print

    public static void main(String args[]){
        long startTime, sieveGeneratedTime, redSieveGeneratedTime, arrayInitialisedTime, 
        primesFoundTime, primesWrittenTime, compressedPrimesWrittenTime,
        //  to find out the time taken for completion of the process
        numbers[] = null, // every number is given 1 bit to store its state 0 means prime
        n;
        int sf;
        App printer = new App(); // multithreading so that main thread doesnt have to wait for stuff to be printed
        printer.start();

        a = new long[64];
        b = new long[64];
 
        for(int i = 0; i < 64; i++){ 
            a[i] = 1l << i;
            b[i] = ~a[i];
        }

        Scanner sc = new Scanner(System.in);
        outStr = outStr + "enter numbers to find primes till:- ";
        prnt = true;
        n = sc.nextLong();
        outStr = outStr + Long.toString(n);
        prnt = true;
        String folderPath = Long.toString(n); // path of folder primesn
        outStr = outStr + "\n" + folderPath;

        outStr = outStr + "\nenter the prime storage factor (not more than 9):- ";
        prnt = true;
        sf = sc.nextInt();
        outStr = outStr + Integer.toString(sf);
        prnt = true;
        sc.close();

        for(int i = 0; i < sf; i++){
            product *= primelist[i];
        }

        startTime = System.currentTimeMillis();
        {
            long sieve[] = new long[(int)(product/64l + 1l)]; //generating the sieve by removing multiples of first sf primes
            outStr = outStr + "\nsieve initialised";
            prnt = true;
            long cp; // current prime

            setState(0, 1, sieve);
            for(int i = 0; i < sf; i++){
                cp = primelist[i];
                for(long j = product/cp; j >= cp; j--){
                    if(getState(j, sieve) == 0l){
                        setState(j * cp, 1, sieve);
                    }
                    else{
                        continue;
                    }
                }
                setState(cp, 1, sieve);
                prntStr = "\nremoved multiples of " + Long.toString(cp) + " from seive";
                prnt = true;
            }

            prntStr = "";
            sieveGeneratedTime = System.currentTimeMillis();
            outStr = outStr + "\nremoved multiples of first " + Integer.toString(sf) + 
            " prime numbers from sieve\nSieve generated";
            prnt = true;

            for(long i = 0; i <= product; i++){
                if(getState(i, sieve) == 0){
                    ncp++;
                }
            }

            int ncp2 = 0; // used to make redSieve
            redSieve = new long[(int)ncp]; //  making the reduced sieve
            for(long i = 0; i <= product; i++){
                if(getState(i, sieve) == 0){
                    redSieve[ncp2] = i;
                    ncp2++;
                }
            }
            redSieveGeneratedTime = System.currentTimeMillis();
            outStr = outStr + "\nreduced sieve generated";
            prnt = true;
        }

        outStr = outStr + "\n" + Long.toString(ncp) + " bits will be used to store state of " 
        + Long.toString(product) + " numbers\nso one bit stores the state of " + 
        Double.toString((double)product / (double)ncp) + " numbers";
        prnt = true;
        
        long numbersLength = (((n - 1l) / product + 1l) * ncp - 1l) / 64l + 1l;
        outStr = outStr + "\nprogram will take " + 
        Double.toString(numbersLength * 0.0000076294) + "mb of ram";
        prnt = true;

        try{
            numbers = new long[(int)numbersLength];
        } 
        catch(OutOfMemoryError e){
            while(prnt){
                continue;
            }
            System.out.print("\narray initialisation failed, too much ram is" + 
            " being used, try using a bigger storage factor");
            System.exit(0);
        }
            

        arrayInitialisedTime = System.currentTimeMillis();
        outStr = outStr + "\nArray initialised\nplease be patient, removing the multiples" + 
        " of first few numbers might take some time";
        prnt = true;

        long nNum, // the value of the last number whose multiples we expect to remove
        nNumPos, // the position of bit of nNum in the numbers list
        nSqrt = (long)Math.sqrt(n);
        nNum = getNearestNum(nSqrt);
        nNumPos = getPos(nNum);

        long iNum; // number at bit pos i in the long list numbers
        for(long i = 1; i <= nNumPos; i++){ // removing all the multiples of expected primes
            if(getState(i, numbers) == 1){
                continue;
            }
            iNum = getNum(i);
            for(long j = getPos(getNearestNum(n / iNum)); j >= i; j--){
                if(getState(j, numbers) == 0){
                    setState(getPos(getNum(j) * iNum), 1, numbers);
                }
                else{
                    continue;
                }
            }
            prntStr = "\nremoved all multiples of " + Long.toString(iNum);
            prnt = true;
        }
        prntStr = "";
        primesFoundTime = System.currentTimeMillis();
        outStr = outStr + "\nremoved all multiples of prime numbers from array\nfound all primes!\nWriting primes to primes" + 
        Long.toString(n) + ".txt";
        prnt = true;

        PrintWriter primestxt = null;
        
        try{
            Files.createDirectory(Paths.get(folderPath));
            primestxt = new PrintWriter(folderPath + "\\primes" + Long.toString(n) + ".txt");
        }
        catch(FileNotFoundException e){
            while(prnt){
                continue;
            }
            System.out.print("\nerror while loading primes" + Long.toString(n) + ".txt");
            System.exit(0);
        }
        catch(IOException e){
            while(prnt){
                continue;
            }
            prnt = false;
            System.out.print("\nerror while creating folder primes" + Long.toString(n) + 
            "\ndeleate earlier instance of folder primes" + Long.toString(n) + " and try again");
            System.exit(0);
        }

        for(int i = 0; i < sf; i++){ // printing all the primes that were not included because of the nature of sieve
            primestxt.print(Long.toString(primelist[i]) + " ,");
        }

        long numberofprimes = sf - 1; /*because the number of numbers flagged in 
                                    numbers list as primes are off by sf - 1 */

        long nearestNNumPos = getPos(getNearestNum(n)); /*the position of the nearest number which is present 
                                                        in numbers list to n which is smaller than or equal to n*/

        setState(0, 1, numbers);
        for(long i = 0; i <= nearestNNumPos; i++){ // writing all the primes in the numbers list
            if(getState(i, numbers) == 0){
                primestxt.print(Long.toString(getNum(i)) + ", ");
                numberofprimes++;
            } 
        }
        primesWrittenTime = System.currentTimeMillis();
        primestxt.close();

        outStr = outStr + "\nwritten all the primes\nwriting compressed primes";
        prnt = true;
        try{
            FileOutputStream fout = new FileOutputStream(folderPath + "\\primes" + Long.toString(n) + "compressed.txt");
            DataOutputStream dout = new DataOutputStream(fout);

            dout.writeLong((long)sf);
            for(int i = 0; i < numbers.length; i++){
                dout.writeLong(numbers[i]);
            }
            dout.close();
            fout.close();
        }
        catch(IOException e){
            while(prnt){
                continue;
            }
            prnt = false;
            System.out.print("\nerror while opennig primes" + Long.toString(n) + "compressed.txt");
            System.exit(0);
        }
        
        compressedPrimesWrittenTime = System.currentTimeMillis();

        prntStr = "";
        outStr = outStr +"\nwritten all the compressed primes" + 
        "\ntotal number of primes found = " + Long.toString(numberofprimes)+ 
        "\ntime taken to generate sive = " + Double.toString(toSeconds(sieveGeneratedTime - startTime)) + 
        "seconds\ntime taken to generate reduced sieve = " + Double.toString(toSeconds(redSieveGeneratedTime - sieveGeneratedTime)) +                       
        "seconds\ntime taken to initialise array = " + Double.toString(toSeconds(arrayInitialisedTime - sieveGeneratedTime)) + 
        "seconds\ntime taken to find primes = " + Double.toString(toSeconds(primesFoundTime - arrayInitialisedTime)) + 
        "seconds\ntime taken to write primes = " + Double.toString(toSeconds(primesWrittenTime - primesFoundTime)) + 
        "seconds\ntime taken to write compressed primes = " + Double.toString(toSeconds(compressedPrimesWrittenTime - primesWrittenTime)) + 
        "seconds\ntotal time taken = " + Double.toString(toSeconds(compressedPrimesWrittenTime - startTime)) + "seconds";
        prnt = true;
    }

    public static double toSeconds(long milTime){
        return (double)milTime / 1000.0;
    }

    public static long setBit(int value, int pos, long num){ // sets the bit at position pos of num to 0 if val equals 0 else 1
        if(value == 0){
            return num & b[pos];
        }
        else{
            return num | a[pos];
        }
    }

    public static long getBit(int pos, long num){ // returns the bit in num at position pos
        if((num & a[pos]) == 0){
            return 0l;
        }
        else {
            return 1l;
        }
    }

    public static void setState(long pos, int value, long[] longList){ // sets the state of a bit in a long in longList
        int longpos = (int)(pos / 64l); // the position of the long to be modified
        int bitpos = (int)(pos - ((pos / 64l) * 64l)); // the position of the bit int the long to be modified
        longList[longpos] = setBit(value, bitpos, longList[longpos]);
    }

    public static long getState(long pos, long[] longList){ // returns the state of a bit in a long in longList
        int longpos = (int)(pos / 64l); // the position of the long to be modified
        int bitpos = (int)(pos - ((pos / 64l) * 64l)); // the position of the bit int the long to be modified
        return getBit(bitpos, longList[longpos]);
    } 

    public static long getPos(long num){ // finds the position of the bit representing num in the numbers list
        long p = num / product; // the position is of the form p * ncp + q
        long q = (long)getPrimePos(num - p * product);
        return p * ncp + q;
    }

    public static long getNum(long pos){ // returns the number at the position pos in the numbers list
        long p = pos / ncp; // the number is of the form p * product + q 
        long q = redSieve[(int)(pos - (p * ncp))];
        return p * product + q;
    }

    public static int getPrimePos(long prime){ // returns the position of prime in the list redSieve
        int first = 0, last = (int)ncp - 1;
        int mid;
        while(first <= last){
            mid = (first + last) / 2;
            if(redSieve[mid] == prime){
                return mid;
            }
            else if(redSieve[mid] > prime){
                last = mid - 1;
            }
            else{
                first = mid + 1;
            }
        }
        return -1; // returns -1 when the number is not found in primeList
    }

    public static long getNearestNum(long num){ // returns the nearest expected prime less than or equal to num
        long ans, h; // ans is of the form product * (some number) + h
        for(ans = num; true; ans--){
            h = ans - product * (ans / product);
            if(getPrimePos(h) != -1){
                break;
            }
        }
        return ans;
    }

    public void run(){ 
        while(true){
            if(prnt){
                System.out.print("\033[H\033[2J");
                System.out.flush();
                System.out.print(outStr + prntStr);
                prnt = false;
            }
        }
    }
}
