package br.com.gilliardcarvalho.todolist.task;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private ITaskRepository taskRepository;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody TaskModel taskModel, HttpServletRequest request) {
        var idUserAttr = request.getAttribute("idUser");
        if (idUserAttr == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não autenticado.");
        }

        final UUID idUser;
        try {
            idUser = UUID.fromString(idUserAttr.toString());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("ID de usuário inválido.");
        }

        taskModel.setIdUser(idUser);

        LocalDateTime now = LocalDateTime.now();
        if (taskModel.getStartAt() != null && now.isAfter(taskModel.getStartAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("A data de início não pode ser no passado.");
        }
        if (taskModel.getStartAt() != null && taskModel.getEndAt() != null
                && taskModel.getEndAt().isBefore(taskModel.getStartAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("A data de término deve ser após a data de início.");
        }

        var task = this.taskRepository.save(taskModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(task);
    }

    @GetMapping
    public ResponseEntity<?> list(HttpServletRequest request) {
        var idUserAttr = request.getAttribute("idUser");
        if (idUserAttr == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não autenticado.");
        }

        final UUID idUser;
        try {
            idUser = UUID.fromString(idUserAttr.toString());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("ID de usuário inválido.");
        }

        var tasks = taskRepository.findByIdUserOrderByStartAtAsc(idUser);
        return ResponseEntity.ok(tasks);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable UUID id,
                                    @RequestBody TaskModel body,
                                    HttpServletRequest request) {
    
        var idUserAttr = request.getAttribute("idUser");
        if (idUserAttr == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não autenticado.");
        } final UUID idUser;
        try {
            idUser = UUID.fromString(idUserAttr.toString());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("ID de usuário inválido.");
        }

        var opt = taskRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Tarefa não encontrada.");
        }

        var task = opt.get();

        if (!task.getIdUser().equals(idUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Sem permissão para editar esta tarefa.");
        }

    if (body.getTitle() != null)       task.setTitle(body.getTitle());
    if (body.getDescription() != null) task.setDescription(body.getDescription());
    if (body.getPriority() != null)    task.setPriority(body.getPriority());
    if (body.getStartAt() != null)     task.setStartAt(body.getStartAt());
    if (body.getEndAt() != null)       task.setEndAt(body.getEndAt());

task.setId(id);
task.setIdUser(idUser);


        
        LocalDateTime now = LocalDateTime.now();
        if (task.getStartAt() != null && now.isAfter(task.getStartAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("A data de início não pode ser no passado.");
        }
        if (task.getStartAt() != null && task.getEndAt() != null
                && task.getEndAt().isBefore(task.getStartAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("A data de término deve ser após a data de início.");
        }

        var saved = this.taskRepository.save(task);
        return ResponseEntity.ok(saved);
    }
}
