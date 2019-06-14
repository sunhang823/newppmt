package com.hangsun.ppmtool.services;

import com.hangsun.ppmtool.domain.Backlog;
import com.hangsun.ppmtool.domain.Project;
import com.hangsun.ppmtool.domain.ProjectTask;
import com.hangsun.ppmtool.exceptions.ProjectNotFoundException;
import com.hangsun.ppmtool.repositories.BacklogRepository;
import com.hangsun.ppmtool.repositories.ProjectRepository;
import com.hangsun.ppmtool.repositories.ProjectTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ProjectTaskService {

    @Autowired
    private BacklogRepository backlogRepository;

    @Autowired
    private ProjectTaskRepository projectTaskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectService projectService;

    public ProjectTask addProjectTask(String projectIdentifier, ProjectTask projectTask, String username){


            //project tasks to be added to a specific project, project != null, backlog exists
            Backlog backlog = projectService.findProjectByIdentifier(projectIdentifier, username).getBacklog(); //backlogRepository.findByProjectIdentifier(projectIdentifier);

            //set the blacklog to project task
            projectTask.setBacklog(backlog);

            //we want our project sequence to be like this: IDPRO-1 IDPRO-2
            Integer BacklogSequence = backlog.getPTSequence();

            //update the backlog sequence
            BacklogSequence++;
            backlog.setPTSequence(BacklogSequence);

            //add sequence to project task
            projectTask.setProjectSequence(projectIdentifier + "-" + BacklogSequence);
            projectTask.setProjectIdentifier(projectIdentifier);

            //initial priority when priority is null
            if(projectTask.getPriority() == null || projectTask.getPriority() == 0) {
                //In the future we need projectTask.getPriority() == 0 to handle the issue
                projectTask.setPriority(3);
            }
            //initial status when status is null
            if(projectTask.getStatus() == "" || projectTask.getStatus() == null) {
                projectTask.setStatus("TO_DO");
            }

            return projectTaskRepository.save(projectTask);

    }

    public Iterable<ProjectTask> findBacklogById(String id, String username) {


        projectService.findProjectByIdentifier(id, username);

        return projectTaskRepository.findByProjectIdentifierOrderByPriority(id);

    }

    public ProjectTask findPTByProjectSequence(String backlog_id, String pt_id, String username) {

        //make sure we are searching on the right backlog
        projectService.findProjectByIdentifier(backlog_id, username);

        //make sure that our task exists
        ProjectTask projectTask = projectTaskRepository.findByProjectSequence(pt_id);
        if(projectTask == null) {
            throw new ProjectNotFoundException("Project Task '" + pt_id + "' not found");
        }

        //make sure that the backlog/project id in the path corresponds to the right project
        if(!projectTask.getProjectIdentifier().equals(backlog_id)) {
            throw new ProjectNotFoundException("Project Task '" + pt_id + "' does not exist in project: " + backlog_id);
        }
        return projectTask;
    }

    public ProjectTask updateByProjectSequence(ProjectTask updatedTask, String backlog_id, String pt_id, String username) {

        ProjectTask projectTask = findPTByProjectSequence(backlog_id, pt_id, username);

        projectTask = updatedTask;

        return projectTaskRepository.save(projectTask);
    }

    public void deletePTByProjectSequence(String backlog_id, String pt_id, String username) {

        ProjectTask projectTask = findPTByProjectSequence(backlog_id, pt_id, username);

        projectTaskRepository.delete(projectTask);
    }


}
