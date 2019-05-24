package com.ipiecoles.java.java230;

import com.ipiecoles.java.java230.exceptions.BatchException;
import com.ipiecoles.java.java230.exceptions.TechnicienException;
import com.ipiecoles.java.java230.model.Commercial;
import com.ipiecoles.java.java230.model.Employe;
import com.ipiecoles.java.java230.model.Manager;
import com.ipiecoles.java.java230.model.Technicien;
import com.ipiecoles.java.java230.repository.EmployeRepository;
import com.ipiecoles.java.java230.repository.ManagerRepository;
import javassist.bytecode.stackmap.BasicBlock;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class MyRunner implements CommandLineRunner {

    private static final String REGEX_MATRICULE = "^[MTC][0-9]{5}$";
    private static final String REGEX_NOM = ".*";
    private static final String REGEX_PRENOM = ".*";
    private static final int NB_CHAMPS_MANAGER = 5;
    private static final int NB_CHAMPS_TECHNICIEN = 7;
    private static final String REGEX_MATRICULE_MANAGER = "^M[0-9]{5}$";
    private static final int NB_CHAMPS_COMMERCIAL = 7;

    @Autowired
    private EmployeRepository employeRepository;

    @Autowired
    private ManagerRepository managerRepository;

    private List<Employe> employes = new ArrayList<>();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void run(String... strings) throws Exception {
        String fileName = "employes.csv";
        readFile(fileName);
        //readFile(strings[0]);
    }

    /**
     * Méthode qui lit le fichier CSV en paramètre afin d'intégrer son contenu en BDD
     *
     * @param fileName Le nom du fichier (à mettre dans src/main/resources)
     * @return une liste contenant les employés à insérer en BDD ou null si le fichier n'a pas pu être le
     */
    public List<Employe> readFile(String fileName) throws Exception {
        Stream<String> stream;
        logger.info("Lecture du fichier" + fileName);

        try {
            stream = Files.lines(Paths.get(new ClassPathResource(fileName).getURI()));
        } catch (IOException e) {
            logger.error("Problème dans l'ouverture du fichier" + fileName);
            return new ArrayList<>();
        }

        //TODO
        List<String> lignes = stream.collect(Collectors.toList());
        logger.info(lignes.size() + "lignes lues");
        for (int i = 0; i < lignes.size(); i++) {
            try {
                processLine(lignes.get(i));
            } catch (BatchException e) {
                logger.error("Ligne " + (i + 1) + " : " + e.getMessage() + " => " + lignes.get(i));
            }
        }
        employeRepository.save(employes);
        return employes;
    }

    /**
     * Méthode qui regarde le premier caractère de la ligne et appelle la bonne méthode de création d'employé
     *
     * @param ligne la ligne à analyser
     * @throws BatchException si le type d'employé n'a pas été reconnu
     */
    private void processLine(String ligne) throws BatchException {
        //TODO
        switch (ligne.substring(0, 1)) {
            case "M":
                processManager(ligne);
                break;
            case "C":
                processCommercial(ligne);
                break;
            case "T":
                processTechnicien(ligne);
                break;
            default:
                throw new BatchException("Type d'employé inconnu " + ligne.substring(0, 1));
        }
    }

    /**
     * Méthode qui crée un Commercial à partir d'une ligne contenant les informations d'un commercial et l'ajoute dans la liste globale des employés
     *
     * @param ligneCommercial la ligne contenant les infos du commercial à intégrer
     * @throws BatchException s'il y a un problème sur cette ligne
     */
    private void processCommercial(String ligneCommercial) throws BatchException {
        //TODO
        Commercial c = new Commercial();
        String[] commercialFields = ligneCommercial.split(",");
        if (commercialFields.length != NB_CHAMPS_COMMERCIAL) {
            throw new BatchException("La ligne commercial ne contient pas " + NB_CHAMPS_COMMERCIAL + " éléments mais " + commercialFields.length);
        }
        fieldsChecker(c, commercialFields);

        try {
            Double.parseDouble(commercialFields[5]);
            c.setCaAnnuel(Double.parseDouble(commercialFields[5]));

        } catch (Exception e) {
            throw new BatchException("Le chiffre d'affaire du commercial est incorrect : " + commercialFields[5]);
        }
        try {
            Integer.parseInt(commercialFields[6]);
        } catch (Exception e) {
            throw new BatchException("La performance du commercial est incorrecte " + commercialFields[6]);
        }
        if (Integer.parseInt(commercialFields[6]) < 0 || Integer.parseInt(commercialFields[6]) > 100) {
            throw new BatchException("La performance du commercial est incorrecte " + commercialFields[6]);
        } else {
            c.setPerformance(Integer.parseInt(commercialFields[6]));
        }
        employes.add(c);
    }

    /**
     * Méthode qui crée un Manager à partir d'une ligne contenant les informations d'un manager et l'ajoute dans la liste globale des employés
     *
     * @param ligneManager la ligne contenant les infos du manager à intégrer
     * @throws BatchException s'il y a un problème sur cette ligne
     */
    private void processManager(String ligneManager) throws BatchException {
        //TODO
        Manager m = new Manager();
        String[] managerFields = ligneManager.split(",");


        if (managerFields.length != NB_CHAMPS_MANAGER) {
            throw new BatchException("La ligne manager ne contient pas " + NB_CHAMPS_MANAGER + " éléments mais " + managerFields.length);
        }
        fieldsChecker(m, managerFields);
        try {
            Double.parseDouble(managerFields[4]);
        } catch (Exception e) {
            throw new BatchException(managerFields[4] + " n'est pas un nombre valide pour un salaire");
        }
        employes.add(m);
    }


    /**
     * Méthode qui crée un Technicien à partir d'une ligne contenant les informations d'un technicien et l'ajoute dans la liste globale des employés
     *
     * @param ligneTechnicien la ligne contenant les infos du technicien à intégrer
     * @throws BatchException s'il y a un problème sur cette ligne
     */
    private void processTechnicien(String ligneTechnicien) throws BatchException {
        //TODO
        String[] technicienFields = ligneTechnicien.split(",");
        Technicien t = new Technicien();
        if (technicienFields.length != NB_CHAMPS_TECHNICIEN) {
            throw new BatchException("La ligne technicien ne contient pas " + NB_CHAMPS_TECHNICIEN + " éléments mais " + technicienFields.length);
        }
        if (!technicienFields[6].matches(REGEX_MATRICULE_MANAGER)) {
            throw new BatchException("la chaîne " + technicienFields[6] + " ne respecte pas l'expression régulière " + REGEX_MATRICULE_MANAGER);
        }
        try {
            Integer.parseInt(technicienFields[5]);

        } catch (Exception e) {
            throw new BatchException("Le grade du technicien est incorrect : " + technicienFields[5]);
        }
        try {
            Integer.parseInt(technicienFields[5]);
            t.setGrade(Integer.parseInt(technicienFields[5]));
        } catch (TechnicienException e) {
            throw new BatchException("Le grade doit être compris entre 1 et 5 : " + technicienFields[5]);
        }
        fieldsChecker(t, technicienFields);
        employes.stream().forEach(emp -> {
            if (emp instanceof Manager && emp.getMatricule() == technicienFields[6]) {
                t.setManager((Manager) emp);
            }
        });
        if (employeRepository.findByMatricule(technicienFields[6]) == null && t.getManager() == null) {
            throw new BatchException("Le manager de matricule " + technicienFields[6] + " n'a pas été trouvé dans le fichier ou en base de données");
        }
        employes.add(t);

    }

    private void fieldsChecker(Employe e, String[] fields) throws BatchException {
        if (!fields[0].matches(REGEX_MATRICULE)) {
            throw new BatchException("la chaîne " + fields[0] + " ne respecte pas l'expression régulière " + REGEX_MATRICULE);
        } else {
            e.setMatricule(fields[0]);
        }

        if (!fields[1].matches(REGEX_NOM)) {
            throw new BatchException("le nom " + fields[1] + " n'est pas valide ");
        } else {
            e.setNom(fields[1]);
        }

        if (!fields[2].matches(REGEX_PRENOM)) {
            throw new BatchException("le prénom " + fields[2] + " n'est pas valide ");
        } else {
            e.setPrenom(fields[2]);
        }

        try {
            DateTimeFormat.forPattern("dd/MM/yyyy").parseLocalDate(fields[3]);
            e.setDateEmbauche(DateTimeFormat.forPattern("dd/MM/yyyy").parseLocalDate(fields[3]));
        } catch (Exception ex) {
            throw new BatchException(fields[3] + " ne respecte pas le format de date dd/MM/yyyy");
        }
        try {
            Double.parseDouble(fields[4]);
            e.setSalaire(Double.parseDouble(fields[4]));
        } catch (Exception ex) {
            throw new BatchException(fields[4] + " n'est pas un nombre valide pour un salaire");
        }

    }


}
