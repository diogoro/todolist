package dev.diogoro.todolist.task;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.mapping.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.diogoro.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private ITaskRepository taskRepository;
    
    @PostMapping("/")
    public ResponseEntity create(@RequestBody TaskModel taskModel, HttpServletRequest request) {
        taskModel.setIdUser((UUID) request.getAttribute("idUser"));
        var now = LocalDateTime.now();
        if (now.isAfter(taskModel.getStartAt()) || now.isAfter(taskModel.getEndAt())) {
            return ResponseEntity.unprocessableEntity().body("A data de inicio / data de témino dever ser maior do que data atual");
        }

        if (taskModel.getStartAt().isAfter(taskModel.getEndAt())) {
            return ResponseEntity.unprocessableEntity().body("A data de inicio dever ser maior do que data de término");
        }
        
        var task = this.taskRepository.save(taskModel);
        return ResponseEntity.ok().body(task);
    }

    @GetMapping("/")
    public ResponseEntity list( HttpServletRequest request) {
        var idUser = (UUID) request.getAttribute("idUser");
        var tasks = this.taskRepository.findByIdUser(idUser);
        return ResponseEntity.ok().body(tasks);
    }

    @PutMapping("/{id}")
    public ResponseEntity update(@RequestBody TaskModel taskModel, HttpServletRequest request, @PathVariable UUID id) {
        var idUser = (UUID) request.getAttribute("idUser");
        var task = this.taskRepository.findById(id).orElse(null);
        if(task == null) {
            return (ResponseEntity) ResponseEntity.notFound();
        }

        if(!task.getIdUser().equals(idUser)) {
            return ResponseEntity.badRequest().body("Usuário não tem permissão para alterar essa tarefa");
        }
        Utils.copyNonNullProperties(taskModel, task);
        var updateResult = this.taskRepository.save(task);
        return ResponseEntity.ok().body(updateResult);
    }
}
