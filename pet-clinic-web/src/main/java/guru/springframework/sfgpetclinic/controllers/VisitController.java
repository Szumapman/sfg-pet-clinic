package guru.springframework.sfgpetclinic.controllers;

import guru.springframework.sfgpetclinic.model.Pet;
import guru.springframework.sfgpetclinic.model.Visit;
import guru.springframework.sfgpetclinic.services.PetService;
import guru.springframework.sfgpetclinic.services.VisitService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.beans.PropertyEditorSupport;
import java.time.LocalDate;
import java.util.Map;

@Controller
public class VisitController {

    private final VisitService visitService;
    private final PetService petService;

    public VisitController(VisitService visitService, PetService petService) {
        this.visitService = visitService;
        this.petService = petService;
    }

    @InitBinder
    public void dataBinder(WebDataBinder dataBinder){
        dataBinder.setDisallowedFields("id");

        dataBinder.registerCustomEditor(LocalDate.class, new PropertyEditorSupport(){
            @Override
            public void setAsText(String text) throws IllegalArgumentException{
                setValue(LocalDate.parse(text));
            }
        });
    }

    @GetMapping("/owners/*/pets/{petId}/visits/new")
    public String initNewVisitForm(@PathVariable("petId") Long petId, Map<String, Object> model){
        Pet pet = petService.findById(petId);
        model.put("pet", pet);
        Visit visit = new Visit();
        pet.getVisits().add(visit);
        visit.setPet(pet);
        model.put("visit", visit);
        return "pets/createOrUpdateVisitForm";
    }

    @PostMapping("/owners/{ownerId}/pets/{petId}/visits/new")
    public String processNewVisitForm(@PathVariable("petId") Long petId, @Valid Visit visit, BindingResult result){
        if(result.hasErrors()){
            return "pets/createOrUpdateVisitForm";
        } else {
            Pet pet = petService.findById(petId);
            pet.getVisits().add(visit);
            visit.setPet(pet);
            visitService.save(visit);
            return "redirect:/owners/{ownerId}";
        }
    }

    @GetMapping("/owners/*/pets/{petId}/visits/{visitId}")
    public String initUpdateVisitForm(@PathVariable("petId") Long petId, @PathVariable("visitId") Long visitId, Map<String, Object> model){
        Pet pet = petService.findById(petId);
        model.put("pet", pet);
        model.put("visit", visitService.findById(visitId));

        return "pets/createOrUpdateVisitForm";
    }

    @PostMapping("/owners/{ownerId}/pets/{petId}/visits/{visitId}")
    public String processUpdateVisitForm(@PathVariable("petId") Long petId, @PathVariable("visitId") Long visitId, @Valid Visit visit, BindingResult result, Model model){
        if(result.hasErrors()){
            model.addAttribute("visit", visit);
            return "pets/createOrUpdateVisitForm";
        } else {
            visit.setId(visitId);
            Pet pet = petService.findById(petId);
            visit.setPet(pet);
            pet.getVisits().remove(visitService.findById(visitId));
            pet.getVisits().add(visit);
            petService.save(pet);
            return "redirect:/owners/{ownerId}";
        }
    }
}
