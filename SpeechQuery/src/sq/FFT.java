package sq;

/*
00002  *  Copyright 2006-2007 Columbia University.
00003  *
00004  *  This file is part of MEAPsoft.
00005  *
00006  *  MEAPsoft is free software; you can redistribute it and/or modify
00007  *  it under the terms of the GNU General Public License version 2 as
00008  *  published by the Free Software Foundation.
00009  *
00010  *  MEAPsoft is distributed in the hope that it will be useful, but
00011  *  WITHOUT ANY WARRANTY; without even the implied warranty of
00012  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
00013  *  General Public License for more details.
00014  *
00015  *  You should have received a copy of the GNU General Public License
00016  *  along with MEAPsoft; if not, write to the Free Software
00017  *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
00018  *  02110-1301 USA
00019  *
00020  *  See the file "COPYING" for the text of the license.
00021  */



 public class FFT {
 
   int n, m;
   
   // Lookup tables.  Only need to recompute when size of FFT changes.
   double[] cos;
   double[] sin;
 
   double[] window;
   
   public FFT(int n) {
     this.n = n;
     this.m = (int)(Math.log(n) / Math.log(2));
 
     // Make sure n is a power of 2
     if(n != (1<<m))
       throw new RuntimeException("FFT length must be power of 2");
 
     // precompute tables
     cos = new double[n/2];
     sin = new double[n/2];
 
//00055 //     for(int i=0; i<n/4; i++) {
//00056 //       cos[i] = Math.cos(-2*Math.PI*i/n);
//00057 //       sin[n/4-i] = cos[i];
//00058 //       cos[n/2-i] = -cos[i];
//00059 //       sin[n/4+i] = cos[i];
//00060 //       cos[n/2+i] = -cos[i];
//00061 //       sin[n*3/4-i] = -cos[i];
//00062 //       cos[n-i]   = cos[i];
//00063 //       sin[n*3/4+i] = -cos[i];        
//00064 //     }
 
     for(int i=0; i<n/2; i++) {
       cos[i] = Math.cos(-2*Math.PI*i/n);
       sin[i] = Math.sin(-2*Math.PI*i/n);
     }
 
     makeWindow();
   }
 
   protected void makeWindow() {
     // Make a blackman window:
     // w(n)=0.42-0.5cos{(2*PI*n)/(N-1)}+0.08cos{(4*PI*n)/(N-1)};
     window = new double[n];
     for(int i = 0; i < window.length; i++)
       window[i] = 0.42 - 0.5 * Math.cos(2*Math.PI*i/(n-1)) 
         + 0.08 * Math.cos(4*Math.PI*i/(n-1));
   }
   
   public double[] getWindow() {
     return window;
   } 
 
   /***************************************************************
00089   * fft.c
00090   * Douglas L. Jones 
00091   * University of Illinois at Urbana-Champaign 
00092   * January 19, 1992 
00093   * http://cnx.rice.edu/content/m12016/latest/
00094   * 
00095   *   fft: in-place radix-2 DIT DFT of a complex input 
00096   * 
00097   *   input: 
00098   * n: length of FFT: must be a power of two 
00099   * m: n = 2**m 
00100   *   input/output 
00101   * x: double array of length n with real part of data 
00102   * y: double array of length n with imag part of data 
00103   * 
00104   *   Permission to copy and use this program is granted 
00105   *   as long as this header is included. 
00106   ****************************************************************/
   public void fft(double[] x, double[] y)
   {
     int i,j,k,n1,n2,a;
     double c,s,e,t1,t2;
   
   
     // Bit-reverse
     j = 0;
     n2 = n/2;
     for (i=1; i < n - 1; i++) {
       n1 = n2;
       while ( j >= n1 ) {
         j = j - n1;
         n1 = n1/2;
       }
       j = j + n1;
     
       if (i < j) {
         t1 = x[i];
         x[i] = x[j];
         x[j] = t1;
         t1 = y[i];
         y[i] = y[j];
         y[j] = t1;
       }
     }
 
     // FFT
     n1 = 0;
     n2 = 1;
   
     for (i=0; i < m; i++) {
       n1 = n2;
       n2 = n2 + n2;
       a = 0;
     
       for (j=0; j < n1; j++) {
         c = cos[a];
         s = sin[a];
         a +=  1 << (m-i-1);
 
         for (k=j; k < n; k=k+n2) {
           t1 = c*x[k+n1] - s*y[k+n1];
           t2 = s*x[k+n1] + c*y[k+n1];
           x[k+n1] = x[k] - t1;
           y[k+n1] = y[k] - t2;
           x[k] = x[k] + t1;
           y[k] = y[k] + t2;
         }
       }
     }
   }                          
 

   // Test the FFT to make sure it's working
//00164   public static void main(String[] args) {
//00165     int N = 8;
//00166 
//00167     FFT fft = new FFT(N);
//00168 
//00169     double[] window = fft.getWindow();
//00170     double[] re = new double[N];
//00171     double[] im = new double[N];
//00172 
//00173     // Impulse
//00174     re[0] = 1; im[0] = 0;
//00175     for(int i=1; i<N; i++)
//00176       re[i] = im[i] = 0;
//00177     beforeAfter(fft, re, im);
//00178 
//00179     // Nyquist
//00180     for(int i=0; i<N; i++) {
//00181       re[i] = Math.pow(-1, i);
//00182       im[i] = 0;
//00183     }
//00184     beforeAfter(fft, re, im);
//00185 
//00186     // Single sin
//00187     for(int i=0; i<N; i++) {
//00188       re[i] = Math.cos(2*Math.PI*i / N);
//00189       im[i] = 0;
//00190     }
//00191     beforeAfter(fft, re, im);
//00192 
//00193     // Ramp
//00194     for(int i=0; i<N; i++) {
//00195       re[i] = i;
//00196       im[i] = 0;
//00197     }
//00198     beforeAfter(fft, re, im);
//00199 
//00200     long time = System.currentTimeMillis();
//00201     double iter = 30000;
//00202     for(int i=0; i<iter; i++)
//00203       fft.fft(re,im);
//00204     time = System.currentTimeMillis() - time;
//00205     System.out.println("Averaged " + (time/iter) + "ms per iteration");
//00206   }
//00207 
//00208   protected static void beforeAfter(FFT fft, double[] re, double[] im) {
//00209     System.out.println("Before: ");
//00210     printReIm(re, im);
//00211     fft.fft(re, im);
//00212     System.out.println("After: ");
//00213     printReIm(re, im);
//00214   }
//00215 
//00216   protected static void printReIm(double[] re, double[] im) {
//00217     System.out.print("Re: [");
//00218     for(int i=0; i<re.length; i++)
//00219       System.out.print(((int)(re[i]*1000)/1000.0) + " ");
//00220 
//00221     System.out.print("]\nIm: [");
//00222     for(int i=0; i<im.length; i++)
//00223       System.out.print(((int)(im[i]*1000)/1000.0) + " ");
//00224 
//00225     System.out.println("]");
//00226   }
 }
