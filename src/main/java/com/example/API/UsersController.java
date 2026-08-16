package com.example.API;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.io.IOException;

import com.example.API.models.*;
import com.example.API.Repositories.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.schema.MongoJsonSchema;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
public class UsersController {

    @Autowired
    private UsersRepository UsersRepository;

    @Autowired
    private TrainersRepository trainersRepository;

    @Autowired
    private ServicesRepository ServicesRepository;

    @Autowired
    private ProgramsRepository ProgramsRepository;

    @Autowired
    private ArticlesRepository ArticlesRepository;

    @Autowired
    private ReservationsRepository reservationsRepository;

    @Autowired
    private MongoTemplate MongoTemplate;

    @PostMapping("/logout")
    public ResponseEntity<String> Logout(HttpServletResponse response){
        Cookie cookie = new Cookie("token", null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(false);

        response.addCookie(cookie);
        return new ResponseEntity<>("Επιτυχής Αποσύνδεση " , HttpStatus.OK);
    }


    @PostMapping("/signup")//save the user in collection
    public ResponseEntity<String> newUser(@RequestBody Users user){
        try{
            String username = user.getUsername();
            Users theuser = UsersRepository.findByUsername(username);
            if(theuser != null){
                return new ResponseEntity<>("Αυτό το Όνομα Χρήστη υπάρχει", HttpStatus.CONFLICT);
            }
            else {
                UsersRepository.save(user);
                return new ResponseEntity<>("Επιτυχής Εγγραφή ", HttpStatus.OK);
            }
        }
        catch(DuplicateKeyException e){
            return new ResponseEntity<>("Username already exist" , HttpStatus.CONFLICT);
        }

    }

    @PostMapping("/login")//Login as user or admin
    public ResponseEntity<String> loginUser(@RequestBody Users user , HttpServletResponse response){
        String username = user.getUsername();
        String password = user.getPassword();
        Users theuser = UsersRepository.findByUsername(username);
        if(theuser != null) {
            if (theuser.getPassword().equals(password)) {
                Cookie cookie = new Cookie("token" , theuser.getId() + "|" + theuser.getRole() + "|" + theuser.getAccepted());
                cookie.setPath("/");
                cookie.setHttpOnly(false);
                cookie.setMaxAge(60*60*24*30);//Expires after 30 days

                response.addCookie(cookie);
                return new ResponseEntity<>("Επιτυχής σύνδεση χρήστη", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Ο Κωδικός Πρόσβασης είναι λάθος", HttpStatus.UNAUTHORIZED);
            }
        }
        else{
            return new ResponseEntity<>("Δεν υπάρχει τέτοιο όνομα Χρήστη" , HttpStatus.UNAUTHORIZED);
        }
    }
//ADMIN
    @GetMapping("/request")//Give all the users with accepted == false
    public ResponseEntity<List<Users>> NotAccepted(){
        List<Users> user_request = UsersRepository.GetNotAccepted();
        return new ResponseEntity<>(user_request , HttpStatus.OK);
    }

    @PostMapping("/request/accepted/{id}")//Accept the user and i choose role
    public ResponseEntity<String> AcceptUser(@PathVariable String id){
        Users user = UsersRepository.findById(id).orElseThrow(() ->
                new NoSuchElementException("User not found with id: " + id));//if the id was not found
        user.setAccepted(true);
        user.setRole("user");
        UsersRepository.save(user);
        return new ResponseEntity<>("Έγκριση Χρήστη επιτυχής", HttpStatus.OK);
    }


    @DeleteMapping("/request/denied/{id}")//Delete the user from the request section and the collection
    public ResponseEntity<String> RejectUser(@PathVariable String id){
        UsersRepository.deleteById(id);
        return new ResponseEntity<>("User rejected and deleted successfully", HttpStatus.OK);
    }

    @GetMapping("/manageusers")//Show all the users that are accepted
    public ResponseEntity<List<Users>> ShowAllUsers(){
        List<Users> all_users = UsersRepository.GetAllAccepted();
        return new ResponseEntity<>(all_users , HttpStatus.OK);
    }

    @PostMapping("/manageusers/change")//Change elements of the user (it needs to have also the id)
    public ResponseEntity<String> ChangeUser(@RequestBody Users user){// Maybe Putmapping
        Users theuser = UsersRepository.findById(user.getId()).orElseThrow(() ->
                new NoSuchElementException("User not found! "));//if the id was not found
        theuser.setName(user.getName());
        theuser.setSurname(user.getSurname());
        theuser.setCountry(user.getCountry());
        theuser.setTown(user.getTown());
        theuser.setAddress(user.getAddress());
        theuser.setEmail(user.getEmail());
        theuser.setUsername(user.getUsername());
        theuser.setRole(user.getRole());
        UsersRepository.save(theuser);
        return new ResponseEntity<>("User successfully updated" , HttpStatus.OK);
    }

    @DeleteMapping("/manageusers/delete/{id}")//Delete the user
    public ResponseEntity<String> DeleteUser(@PathVariable String id){
        UsersRepository.deleteById(id);
        return new ResponseEntity<>("User deleted successfully" , HttpStatus.OK);
    }
//Admin Services
//Trainers
    @GetMapping("/manageservices/trainers")//show all the trainers
    public ResponseEntity<List<Trainers>> ShowAllTrainers(){
        List<Trainers> all_trainers = trainersRepository.findAll();
        return new ResponseEntity<>(all_trainers , HttpStatus.OK);
    }

    @GetMapping("/manageservices/trainers/{id}")//show specific trainer
    public ResponseEntity<Optional<Trainers>> ShowTrainer(@PathVariable String id){
        Optional<Trainers> trainer = trainersRepository.findById(id);
        return new ResponseEntity<>(trainer , HttpStatus.OK);
    }
    @PostMapping("/manageservices/trainers/create")//Create new trainer
    public ResponseEntity<String> NewTrainer(@RequestBody Trainers trainer){
        if (trainersRepository == null) {
            System.out.println("TrainersRepository is NULL!");
            return new ResponseEntity<>("Internal Server Error: Repository not initialized", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        try{
            trainersRepository.save(trainer);
            return new ResponseEntity<>("Επιτυχής Εγγραφή Γυμναστή", HttpStatus.OK);
        }
        catch(DuplicateKeyException e){
            return new ResponseEntity<>("Trainer already exist" , HttpStatus.CONFLICT);
        }

    }

    @PostMapping("/manageservices/trainers/change")//Change trainer elements
    public ResponseEntity<String> ChangeTrainer(@RequestBody Trainers trainer){
        Trainers thetrainer = trainersRepository.findById(trainer.getId()).orElseThrow(() ->
                new NoSuchElementException("Trainer not found! "));//if the id was not found
        thetrainer.setName(trainer.getName());
        thetrainer.setSurname(trainer.getSurname());
        trainersRepository.save(thetrainer);
        return new ResponseEntity<>("Trainer successfully updated" , HttpStatus.OK);
    }

    @DeleteMapping("/manageservices/trainers/delete/{id}")//Delete trainer
    public ResponseEntity<String> DeleteTrainer(@PathVariable String id){
        trainersRepository.deleteById(id);
        return new ResponseEntity<>("Trainer deleted successfully" , HttpStatus.OK);
    }
//Services
    @GetMapping("/manageservices/services")//Show all the gym's programms
    public ResponseEntity<List<Services>> ShowAllServices(){
        List<Services> all_services = ServicesRepository.findAll();
        return new ResponseEntity<>(all_services , HttpStatus.OK);
    }

    @PostMapping(path = "/uploadimage" , consumes = MediaType.MULTIPART_FORM_DATA_VALUE)//upload the image
    public ResponseEntity<Map<String, String>> upload_image(@RequestParam("image") MultipartFile imageFile){
        try {
            String upload_dir = "src/main/resources/static/Images";
            Path path_upload = Paths.get(upload_dir);
            if (!Files.exists(path_upload)) {
                Files.createDirectories(path_upload);
            }
            String originalFilename = StringUtils.cleanPath(imageFile.getOriginalFilename());
            String filename = UUID.randomUUID().toString() + "_" + originalFilename;
            Path filePath = path_upload.resolve(filename);
            Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Construct the URL (adjust as per your static resource mapping)
            String imageUrl = "../../Images/" + filename;
            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", imageUrl);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(Map.of("error", "Image upload failed"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PostMapping("/manageservices/services/create")//Create new service
    public ResponseEntity<String> NewService(@RequestBody Services service){
        try{
            ServicesRepository.save(service);
            return new ResponseEntity<>("Επιτυχής Εγγραφή Υπηρεσίας", HttpStatus.OK);
        }
        catch(DuplicateKeyException e){
            return new ResponseEntity<>("Service already exist" , HttpStatus.CONFLICT);
        }

    }

    @PostMapping("/manageservices/services/change")//Change elements of the service
    public ResponseEntity<String> ChangeService(@RequestBody Services service){
        Services theservice = ServicesRepository.findById(service.getId()).orElseThrow(() ->
                new NoSuchElementException("Trainer not found! "));//if the id was not found
        theservice.setName(service.getName());
        theservice.setDescription(service.getDescription());
        theservice.setPrice(service.getPrice());
        theservice.setMax_people(service.getMax_people());
        ServicesRepository.save(theservice);
        return new ResponseEntity<>("Service successfully updated" , HttpStatus.OK);
    }

    @DeleteMapping("/manageservices/services/delete/{id}")//Delete service
    public ResponseEntity<String> DeleteService(@PathVariable String id){
        ServicesRepository.deleteById(id);
        return new ResponseEntity<>("Service deleted successfully" , HttpStatus.OK);
    }
//Schedule of services
    @GetMapping("/manageservices/program")//Show all the programs
    public  ResponseEntity<List<Programs>> ShowAllProgram(){
        List<Programs> all_program = ProgramsRepository.findAll();
        return new ResponseEntity<>(all_program , HttpStatus.OK);
    }

    @GetMapping("/manageservices/program/futures")//Show all the program from today and after
    public ResponseEntity<List<Programs>> ShowFutureProgram(){
        LocalDate today = LocalDate.now();
        Date todayy = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Query query = new Query();
        query.addCriteria(Criteria.where("programdate").gte(todayy));
        List<Programs> all_program = MongoTemplate.find(query, Programs.class);
        return new ResponseEntity<>(all_program, HttpStatus.OK);
    }

    @PostMapping("/manageservices/program/create")//Create new Schedule
    public ResponseEntity<String> NewProgram(@RequestBody Programs program){
        //Check if it creates conflict with some record
        Query query = new Query();
        query.addCriteria(Criteria.where("trainer_id").is(program.getTrainer_id()));
        query.addCriteria(Criteria.where("programdate").is(program.getProgramdate()));
        List<Programs> sametime_program = MongoTemplate.find(query, Programs.class);
        int new_time = program.getHour().toSecondOfDay() / 60;//Convert to minutes
        for (Programs current_program : sametime_program) {
            int currentprogram_minutes = current_program.getHour().toSecondOfDay() / 60;
            int dif = Math.abs(new_time - currentprogram_minutes);
            if (dif < 60) {//If it has something already
                return new ResponseEntity<>("Ο συγκεκριμένος γυμναστής ήδη κάνει κάτι.", HttpStatus.CONFLICT);
            }
        }
        ProgramsRepository.save(program);
        return new ResponseEntity<>("Επιτυχής εγγραφή στο πρόγραμμα", HttpStatus.OK);
    }

    @PostMapping("/manageservices/program/change")//Change elements of the program
    public ResponseEntity<String>  ChangeProgram(@RequestBody Programs program){
        Programs theprogram = ProgramsRepository.findById(program.getId()).orElseThrow(() ->
                new NoSuchElementException("Trainer not found! "));//if the id was not found
        theprogram.setServices_name(program.getServices_name());
        theprogram.setHour(program.getHour());
        theprogram.setMax_people(program.getMax_people());
        theprogram.setProgramdate(program.getProgramdate());
        theprogram.setTrainer_id(program.getTrainer_id());
        ProgramsRepository.save(theprogram);
        return new ResponseEntity<>("Program successfully updated" , HttpStatus.OK);
    }

    @DeleteMapping("/manageservices/program/delete/{id}")//Delete specific part of the program
    public ResponseEntity<String> DeleteProgram(@PathVariable("id") String id){
        ProgramsRepository.deleteById(id);
        return new ResponseEntity<>("the specific schedule deleted successfully" , HttpStatus.OK);
    }
//Announcements and Discounts
    @GetMapping("/managearticles")//Show all the articles and the discounts
    public ResponseEntity<List<Articles>> ShowAllArticles(){//to be sorted by when they created (as bonus)
        List<Articles> all_articles = ArticlesRepository.findAll();
        return new ResponseEntity<>(all_articles , HttpStatus.OK);
    }

    @GetMapping("/managearticles/{id}")//Show all the articles and the discounts
    public ResponseEntity<Articles> ShowArticle(@PathVariable String id){//to be sorted by when they created (as bonus)
        Articles articles = ArticlesRepository.findById(id).orElseThrow(() ->
                new NoSuchElementException("Trainer not found! "));
        return new ResponseEntity<>(articles , HttpStatus.OK);
    }

    @PostMapping("/managearticles/create")//Create new articles
    public ResponseEntity<String> NewArticle(@RequestBody Articles article){
        try{
            ArticlesRepository.save(article);
            return new ResponseEntity<>("Επιτυχής Εγγραφή Ανακοίνωσης", HttpStatus.OK);
        }
        catch(DuplicateKeyException e){
            return new ResponseEntity<>("Service already exist" , HttpStatus.CONFLICT);
        }
    }

    @PostMapping("/managearticles/change")//Change specific article
    public ResponseEntity<String> ChangeArticle(@RequestBody Articles article){
        Articles thearticle = ArticlesRepository.findById(article.getId()).orElseThrow(() ->
                new NoSuchElementException("Trainer not found! "));//if the id was not found
        thearticle.setTitle(article.getTitle());
        thearticle.setBody(article.getBody());
        ArticlesRepository.save(thearticle);
        return new ResponseEntity<>("Article successfully updated" , HttpStatus.OK);
    }

    @DeleteMapping("/managearticles/delete/{id}")//Delete specific article
    public ResponseEntity<String> DeleteArticle(@PathVariable String id){
        ArticlesRepository.deleteById(id);
        return new ResponseEntity<>("the article deleted successfully" , HttpStatus.OK);
    }

//USER
    @GetMapping("/service/show/{service_name}")//Show the weekly program of the specific service
    public ResponseEntity<List<Programs>>  ShowProgramsofService(@PathVariable String service_name){
        List<Programs> services_program = ProgramsRepository.showProgram(service_name);
        return new ResponseEntity<>(services_program , HttpStatus.OK);
    }
//B.2
    @GetMapping("/reservationsavailable/{userid}")//Show all the reservations available
    public ResponseEntity<List<Programs>> ShowAllAvailableRes(@PathVariable String userid){
        List<Programs> available_program = ProgramsRepository.ShowAllAvailable();
        List<UserReservations> userReservations = reservationsRepository.findReservationsByUserid(userid);

        // Create a set of program IDs that the user has already reserved
        Set<String> reservedPrograms = userReservations.stream()
                .map(UserReservations::getProgramid)
                .collect(Collectors.toSet());

        // Filter out programs that the user has already reserved
        List<Programs> filteredPrograms = available_program.stream()
                .filter(program -> !reservedPrograms.contains(program.getId()))
                .collect(Collectors.toList());
        return new ResponseEntity<>(filteredPrograms , HttpStatus.OK);
    }

    @GetMapping("/reservationuser/{userid}")//Show all the reservations that user has made for future
    public ResponseEntity<List<Programs>> ShowNewReservations(@PathVariable String userid){
        List<UserReservations> all_userreservations = UsersRepository.findByUserid(userid);

        Set<String> program_id = all_userreservations.stream()
                .map(UserReservations::getProgramid)
                .collect(Collectors.toSet());
        Date now = new Date();
        Query query = new Query();
        query.addCriteria(Criteria.where("id").in(program_id));
        query.addCriteria(Criteria.where("programdate").gt(now));

        List<Programs> futurePrograms = MongoTemplate.find(query, Programs.class);

        return new ResponseEntity<>(futurePrograms, HttpStatus.OK);
    }

    @PostMapping("/reservations/create")//Create new reservation
    public ResponseEntity<String> NewReservation(@RequestBody UserReservations res){
        reservationsRepository.save(res);
        Programs theprogram = ProgramsRepository.findById(res.getProgramid()).orElseThrow(() ->
                new NoSuchElementException("Trainer not found! "));//if the id was not found
        theprogram.addpeople();
        ProgramsRepository.save(theprogram);
        return new ResponseEntity<>("Reservation submitted" , HttpStatus.OK);
    }

    @DeleteMapping("/reservations/delete/{userid}/{programid}")//Cancel the specific reservation
    public ResponseEntity<String> DeleteReservation(@PathVariable String userid , @PathVariable String programid){
        UserReservations thereservation = reservationsRepository.findByUseridAndProgramid(userid , programid);
        if (thereservation == null) {
            return new ResponseEntity<>("Reservation not found", HttpStatus.NOT_FOUND);
        }
        LocalDate today = LocalDate.now();
        Programs theprogram = ProgramsRepository.findById(thereservation.getProgramid()).orElseThrow(() ->
                new NoSuchElementException("Trainer not found! "));//if the id was not found
        LocalDate program_date = theprogram.getProgramdate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        if(program_date.isBefore(today)){
            return new ResponseEntity<>("Δεν μπορεί να διαγραφεί η κράτηση" , HttpStatus.BAD_REQUEST);
        }
        if (program_date.equals(today)) {
            LocalTime program_time = theprogram.getHour();
            LocalTime now_plus2 = LocalTime.now().plusHours(2);
            if (program_time.isBefore(now_plus2)){
                return new ResponseEntity<>("Δεν μπορεί να διαγραφεί η κράτηση" , HttpStatus.BAD_REQUEST);
            }
        }
        reservationsRepository.deleteById(thereservation.getId());
        theprogram.removepeople();
        ProgramsRepository.save(theprogram);
        return new ResponseEntity<>("Επυτιχής διαγραφή κράτησης" , HttpStatus.OK);
    }

//B.3
    @GetMapping("/reservations/{id}")//Show all the reservations of the user
    public ResponseEntity<List<Programs>> ShowAllUserReservations(@PathVariable String id){
        List<UserReservations> all_userreservations = UsersRepository.findByUserid(id);

        List<Programs> user_programs = new ArrayList<>();

        for (UserReservations res : all_userreservations) {
            Optional<Programs> program = ProgramsRepository.findById(res.getProgramid());
            program.ifPresent(user_programs::add);
        }
        return new ResponseEntity<>(user_programs, HttpStatus.OK);
    }
}
