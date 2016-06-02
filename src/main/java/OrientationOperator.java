import org.json.JSONObject;

/**
 * Created by vitaly on 02.06.16.
 */
public class OrientationOperator {
    private final int constG = 16384;
    private final double calibrationKoef = 1.0;

    double[][] matrixD;
    double[][] matrixDDot;

    public OrientationOperator() {
        this.matrixD = new double[4][4];
        this.matrixDDot = new double[4][4];
    }

    public void addData(JSONObject jsonObject){
        double[][] matrixOmega = new double[4][4];

        matrixOmega[1][2] = jsonObject.getInt("gz") / 131.0;
        matrixOmega[1][3] = - jsonObject.getInt("gy") / 131.0;

        matrixOmega[2][1] = - jsonObject.getInt("gz") / 131.0;
        matrixOmega[2][3] = jsonObject.getInt("gx") / 131.0;

        matrixOmega[3][1] = jsonObject.getInt("gy") / 131.0;
        matrixOmega[3][2] = - jsonObject.getInt("gx") / 131.0;

        double deltaD13 = matrixD[1][3] - jsonObject.getInt("ax") / constG;
        double deltaD23 = matrixD[2][3] - jsonObject.getInt("ay") / constG;
        double deltaD33 = matrixD[3][3] - jsonObject.getInt("az") / constG;

        matrixDDot[1][1] = matrixOmega[1][2] * matrixD[2][1] - matrixOmega[3][1] * matrixD[3][1];
        matrixDDot[1][2] = matrixOmega[1][2] * matrixD[2][2] - matrixOmega[3][1] * matrixD[3][2];
        matrixDDot[1][3] = matrixOmega[1][2] * matrixD[2][3] - matrixOmega[3][1] * matrixD[3][3] - calibrationKoef * deltaD13;

        matrixDDot[2][1] = - matrixOmega[1][2] * matrixD[1][1] + matrixOmega[2][3] * matrixD[3][1];
        matrixDDot[2][2] = - matrixOmega[1][2] * matrixD[1][2] + matrixOmega[2][3] * matrixD[3][2];
        matrixDDot[2][3] = - matrixOmega[1][2] * matrixD[1][3] + matrixOmega[2][3] * matrixD[3][3] - calibrationKoef * deltaD23;

        matrixDDot[3][1] = matrixOmega[3][1] * matrixD[1][1] - matrixOmega[2][3] * matrixD[2][1];
        matrixDDot[3][2] = matrixOmega[3][1] * matrixD[1][2] - matrixOmega[2][3] * matrixD[2][2];
        matrixDDot[3][3] = matrixOmega[3][1] * matrixD[1][3] - matrixOmega[2][3] * matrixD[2][3] - calibrationKoef * deltaD33;

        for (int i = 1; i < 4; i++){
            for (int j = 1; j < 4; j++){
                matrixD[i][j] += (matrixDDot[i][j] * 6.4 / 1000);
            }
        }

        double gamma = - Math.atan(matrixD[1][3] / matrixD[3][3]) / Math.PI * 180.0;
        double tetta = Math.atan(matrixD[2][3] / Math.sqrt(matrixD[2][1] * matrixD[2][1] + matrixD[2][2] * matrixD[2][2] )) / Math.PI * 180.0;

        System.out.println(String.format("gamma: %f tetta %f ", gamma, tetta));

    }

}
