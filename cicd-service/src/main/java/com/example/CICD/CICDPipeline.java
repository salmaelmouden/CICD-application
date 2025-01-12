package com.example.CICD;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class CICDPipeline {

    public static void main(String[] args) {
        try {
            File directory = new File("CICD-application");
            if (directory.exists()) {
                // Si le répertoire existe, supprimez-le
                executeCommand("rm -rf CICD-application");
                // executeCommand("rmdir /s /q CICD-application");
            } else {
                System.out.println("Le répertoire CICD-application n'existe pas, aucune suppression nécessaire.");
            }

            
            // Étape 1: Clonage du dépôt GitHub
            executeCommand("git clone -b main https://github.com/salmaelmouden/CICD-application.git");

            Thread.sleep(2000); // 2 secondes

            // Étape 2: Compilation Maven avec exécution des tests unitaires
            executeCommand("cd CICD-application && mvn clean install"); 
            
            // executeCommand("mvn clean install");

            // // Étape 3: Analyse de code avec SonarQube
            // executeCommand("cd CICD-application && mvn sonar:sonar " +
            //     "-Dsonar.projectKey=CICD-application " +
            //     "-Dsonar.host.url=http://sonarqube-server:9000 " +
            //     "-Dsonar.login=your-sonar-token");
            Thread.sleep(2000); // 2 secondes

            // Étape 4: Création de l'image Docker
            
            executeCommand("docker rm -f mongodb || true");
            executeCommand("docker rm -f /auth-service || true");
            executeCommand("docker rm -f /cicd-service || true");
            executeCommand("cd CICD-application && docker-compose up -d");
            // executeCommand("cd CICD-application && docker build -t cicd-application:latest .");


            // Étape 5: Déploiement sur la VM via SSH
            String sshCommand = "ssh user@192.168.1.100 \"docker stop app || true && " +
                "docker rm app || true && " +
                "docker run -d --name app -p 8080:8080 cicd-application:latest\"";
            executeCommand(sshCommand);

            // Étape 6: Exécution des tests d'intrusion avec Burp Suite
            executeCommand("java -jar /path/to/burpsuite_pro.jar " +
                "--project-file=/tmp/burp-project.burp " +
                "--config-file=/path/to/config.json " +
                "--scan-target=http://192.168.1.100:8080 " +
                "--output-format=HTML --output-file=burp-report.html");

            // Étape 7: Notification de succès
            System.out.println("Pipeline CI/CD exécuté avec succès !");
        } catch (Exception e) {
            System.err.println("Erreur dans le pipeline : " + e.getMessage());
            try {
                // Rollback en cas d'échec
                System.out.println("Exécution du rollback...");
                String rollbackCommand = "ssh user@192.168.1.100 \"docker stop app || true && " +
                    "docker rm app || true && " +
                    "docker pull cicd-application:stable && " +
                    "docker run -d --name app -p 8080:8080 cicd-application:stable\"";
                executeCommand(rollbackCommand);
                System.out.println("Rollback exécuté avec succès.");
            } catch (Exception rollbackException) {
                System.err.println("Erreur pendant le rollback : " + rollbackException.getMessage());
            }
        }
    }

    /**
     * Exécute une commande shell et affiche les logs.
     */
    private static void executeCommand(String command) throws Exception {
        Process process;
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            // Sur Windows, utilisez cmd.exe pour exécuter la commande
            process = Runtime.getRuntime().exec("cmd.exe /c " + command);
        } else {
            // Sur Linux/MacOS, utilisez directement la commande
            process = Runtime.getRuntime().exec(command.split(" "));
        }
    
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
    
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
        while ((line = errorReader.readLine()) != null) {
            System.err.println(line);
        }
    
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Commande échouée : " + command);
        }
    }
}
