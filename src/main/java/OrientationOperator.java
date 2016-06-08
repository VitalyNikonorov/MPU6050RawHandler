import org.json.JSONObject;

/**
 * Created by vitaly on 02.06.16.
 */
public class OrientationOperator {

    private double[][] matrixD;
    private double[][] D1;
    private final int constG = 16384;
    private final int constW = 131;
    private final double calibrKoef = 20;
    private final double deltaT = 0.0064; //seconds
    int num = 0;
    double S;
    double psy = 0.0;

    public OrientationOperator() {
        matrixD = new double[4][4];
        matrixD[1][1] = 1.0;
        matrixD[2][2] = 1.0;
        matrixD[3][3] = 1.0;

        D1 = new double[4][4];
        D1[1][1] = 1.0;
        D1[2][2] = 1.0;
        D1[3][3] = 1.0;
    }

    public void addData(JSONObject jsonObject){

        num++;

        //calibrate
        double wx = ((jsonObject.getInt("gx") + 332) / (1 + 0.031)) / constW;
        double wy = (jsonObject.getInt("gy") + 91) / (1 + 0.0034) / constW;
        double wz = (jsonObject.getInt("gz") - 184) / (1 + 0.013) / constW;

        double ax = ((jsonObject.getInt("ax") - 150) / (1 + 0.0479)) / constG * 9.81; // m/c^2  //547
        double ay = ((jsonObject.getInt("ay") - 5) / (1 + 0.0022)) / constG * 9.81; // m/c^2
        double az = ((jsonObject.getInt("az") + 662) / (1 + 0.0217)) / constG * 9.81; // m/c^2
        //calibrated

        //body to local level

        double fE = matrixD[1][1] * ax + matrixD[1][2] * ay + matrixD[1][3] * az;
        double fN = matrixD[2][1] * ax + matrixD[2][2] * ay + matrixD[2][3] * az;
        //double fUp = matrixD[3][1] * ax + matrixD[3][2] * ay + matrixD[3][3] * az;

        //Puasson
        double wEC = - calibrKoef * fN; //-
        double wNC =   calibrKoef * fE;
        double wUpC = - psy / deltaT / 1000.0;


//        D1[1][1] = matrixD[1][1] + ( - matrixD[1][3] * wy - matrixD[3][1] * wNC + matrixD[2][1] * wUpC) * deltaT;
////        D1[1][2] = 0.0;//matrixD[1][2] + (matrixD[1][3] * wx - matrixD[3][2] * wNC) * deltaT;
//        D1[1][2] = matrixD[1][2] + (matrixD[1][3] * wx - matrixD[3][2] * wNC + matrixD[2][2] * wUpC) * deltaT;
//        D1[1][3] = matrixD[1][3] + (matrixD[1][1] * wy - matrixD[1][2] * wx - matrixD[3][3] * wNC + matrixD[2][3] * wUpC) * deltaT;
//
//        D1[2][1] = matrixD[2][1] + ( - matrixD[2][3] * wy + matrixD[3][1] * wEC - matrixD[1][1] * wUpC) * deltaT;
//        D1[2][2] = matrixD[2][2] + (matrixD[2][3] * wx + matrixD[3][2] * wEC - matrixD[1][2] * wUpC) * deltaT;
//        D1[2][3] = matrixD[2][3] + (matrixD[2][1] * wy - matrixD[2][2] * wx + matrixD[3][3] * wEC - matrixD[1][3] * wUpC) * deltaT;
//
//        D1[3][1] = matrixD[3][1] + ( - matrixD[3][3] * wy + matrixD[1][1] * wNC - matrixD[2][1] * wEC) * deltaT;
//        D1[3][2] = matrixD[3][2] + (matrixD[3][3] * wx + matrixD[1][2] * wNC - matrixD[2][2] * wEC) * deltaT;
//        D1[3][3] = matrixD[3][3] + (matrixD[3][1] * wy - matrixD[3][2] * wx + matrixD[1][3] * wNC - matrixD[2][3] * wEC) * deltaT;

        D1[1][1] = matrixD[1][1] + deltaT * ( matrixD[1][2] * wz - matrixD[1][3] * wy + matrixD[2][1] * wUpC - matrixD[3][1] * wNC );
        D1[1][2] = matrixD[1][2] + deltaT * ( matrixD[1][3] * wx - matrixD[1][1] * wz + matrixD[2][2] * wUpC - matrixD[3][2] * wNC );
        D1[1][3] = matrixD[1][3] + deltaT * ( matrixD[1][1] * wy - matrixD[1][2] * wx + matrixD[2][3] * wUpC - matrixD[3][3] * wNC );

        D1[2][1] = matrixD[2][1] + deltaT * ( matrixD[2][2] * wz - matrixD[2][3] * wy - matrixD[1][1] * wUpC + matrixD[3][1] * wEC );
        D1[2][2] = matrixD[2][2] + deltaT * ( matrixD[2][3] * wx - matrixD[2][1] * wz - matrixD[1][2] * wUpC + matrixD[3][2] * wEC );
        D1[2][3] = matrixD[2][3] + deltaT * ( matrixD[2][1] * wy - matrixD[2][2] * wx - matrixD[1][3] * wUpC + matrixD[3][3] * wEC );

        D1[3][1] = matrixD[3][1] + deltaT * ( matrixD[3][2] * wz - matrixD[3][3] * wy + matrixD[1][1] * wNC - matrixD[2][1] * wEC );
        D1[3][2] = matrixD[3][2] + deltaT * ( matrixD[3][3] * wx - matrixD[3][1] * wz + matrixD[1][2] * wNC - matrixD[2][2] * wEC );
        D1[3][3] = matrixD[3][3] + deltaT * ( matrixD[3][1] * wy - matrixD[3][2] * wx + matrixD[1][3] * wNC - matrixD[2][3] * wEC );

        // end Puasson

        // normalize
        S = 1.0;

        if (num % 2 == 1){
            //line
            S = Math.sqrt(D1[1][1] * D1[1][1] + D1[1][2] * D1[1][2] + D1[1][3] * D1[1][3]);
            matrixD[1][1] = D1[1][1] / S;
            matrixD[1][2] = D1[1][2] / S;
            matrixD[1][3] = D1[1][3] / S;

            S = Math.sqrt(D1[2][1] * D1[2][1] + D1[2][2] * D1[2][2] + D1[2][3] * D1[2][3]);
            matrixD[2][1] = D1[2][1] / S;
            matrixD[2][2] = D1[2][2] / S;
            matrixD[2][3] = D1[2][3] / S;

            S = Math.sqrt(D1[3][1] * D1[3][1] + D1[3][2] * D1[3][2] + D1[3][3] * D1[3][3]);
            matrixD[3][1] = D1[3][1] / S;
            matrixD[3][2] = D1[3][2] / S;
            matrixD[3][3] = D1[3][3] / S;
        } else {
            //column
            S = Math.sqrt(D1[1][1] * D1[1][1] + D1[2][1] * D1[2][1] + D1[3][1] * D1[3][1]);
            matrixD[1][1] = D1[1][1] / S;
            matrixD[2][1] = D1[2][1] / S;
            matrixD[3][1] = D1[3][1] / S;

            S = Math.sqrt(D1[1][2] * D1[1][2] + D1[2][2] * D1[2][2] + D1[3][2] * D1[3][2]);
            matrixD[1][2] = D1[1][2] / S;
            matrixD[2][2] = D1[2][2] / S;
            matrixD[3][2] = D1[3][2] / S;

            S = Math.sqrt(D1[1][3] * D1[1][3] + D1[2][3] * D1[2][3] + D1[3][3] * D1[3][3]);
            matrixD[1][3] = D1[1][3] / S;
            matrixD[2][3] = D1[2][3] / S;
            matrixD[3][3] = D1[3][3] / S;
        }

        // end normalize

        // calculate angles

//        double gamma = - Math.atan(matrixD[1][3] / matrixD[3][3]) / Math.PI * 180.0;
//        double tetta = Math.atan(matrixD[2][3] / Math.sqrt(matrixD[1][3] * matrixD[1][3] + matrixD[3][3] * matrixD[3][3] )) / Math.PI * 180.0;
//        double psy = Math.atan(matrixD[2][1] / matrixD[2][2]) / Math.PI * 180.0;


        double C0 = Math.sqrt(matrixD[3][1] * matrixD[3][1] + matrixD[3][3] * matrixD[3][3]);

        double tetta = Math.atan(matrixD[3][2] / C0) / Math.PI * 180.0;
        double gamma = - Math.atan(matrixD[3][1] / matrixD[3][3]) / Math.PI * 180.0;
        psy = Math.atan(matrixD[1][2] / matrixD[2][2]) * Math.PI * 180.0;

        System.out.println(String.format("gamma:\t%f\ttetta\t%f\tpsy:\t%f", gamma, tetta, psy));

//        for (int i = 1; i <= 3; i++){
//            for (int j = 1; j <= 3; j++){
//                System.out.print(matrixD[i][j]+"\t");
//            }
//        }
//        System.out.println();

    }

}
