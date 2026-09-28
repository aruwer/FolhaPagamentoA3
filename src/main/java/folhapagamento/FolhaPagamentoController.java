package folhapagamento;

import java.io.IOException;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping
public class FolhaPagamentoController {
    private final FolhaPagamentoService service;

    public FolhaPagamentoController(FolhaPagamentoService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String inicio(Model model) {
        model.addAttribute("colaboradores", service.listar());
        model.addAttribute("resumo", service.gerarResumo());
        return "index";
    }

    @GetMapping("/colaboradores/novo")
    public String novo(Model model) {
        return exibirFormulario(model, new ColaboradorForm(), false);
    }

    @PostMapping("/colaboradores")
    public String cadastrar(@Valid @ModelAttribute("form") ColaboradorForm form, BindingResult resultado,
            Model model, RedirectAttributes redirect) throws IOException {
        if (resultado.hasErrors()) {
            return exibirFormulario(model, form, false);
        }
        try {
            service.cadastrar(form.construirColaborador());
            redirect.addFlashAttribute("sucesso", "Colaborador cadastrado.");
            return "redirect:/";
        } catch (IllegalArgumentException erro) {
            resultado.rejectValue("matricula", "matricula.duplicada", erro.getMessage());
            return exibirFormulario(model, form, false);
        }
    }

    @GetMapping("/colaboradores/{matricula}/editar")
    public String editar(@PathVariable String matricula, Model model, RedirectAttributes redirect) {
        return service.buscar(matricula).map(colaborador -> exibirFormulario(model,
                ColaboradorForm.de(colaborador), true)).orElseGet(() -> {
                    redirect.addFlashAttribute("erro", "Colaborador não encontrado.");
                    return "redirect:/";
                });
    }

    @PostMapping("/colaboradores/{matricula}")
    public String atualizar(@PathVariable String matricula,
            @Valid @ModelAttribute("form") ColaboradorForm form, BindingResult resultado,
            Model model, RedirectAttributes redirect) throws IOException {
        form.setMatricula(matricula);
        if (resultado.hasErrors()) {
            return exibirFormulario(model, form, true);
        }
        try {
            service.atualizar(form.construirColaborador());
            redirect.addFlashAttribute("sucesso", "Cadastro atualizado.");
            return "redirect:/";
        } catch (IllegalArgumentException erro) {
            model.addAttribute("erro", erro.getMessage());
            return exibirFormulario(model, form, true);
        }
    }

    @PostMapping("/colaboradores/{matricula}/excluir")
    public String excluir(@PathVariable String matricula, RedirectAttributes redirect) throws IOException {
        try {
            service.excluir(matricula);
            redirect.addFlashAttribute("sucesso", "Colaborador excluído.");
        } catch (IllegalArgumentException erro) {
            redirect.addFlashAttribute("erro", erro.getMessage());
        }
        return "redirect:/";
    }

    @GetMapping("/folha")
    public String folha(Model model) {
        model.addAttribute("colaboradores", service.listar());
        model.addAttribute("resumo", service.gerarResumo());
        return "folha";
    }

    @GetMapping("/folha/{matricula}")
    public String folhaIndividual(@PathVariable String matricula, Model model, RedirectAttributes redirect) {
        return service.buscar(matricula).map(colaborador -> {
            model.addAttribute("colaborador", colaborador);
            return "folha-individual";
        }).orElseGet(() -> {
            redirect.addFlashAttribute("erro", "Colaborador não encontrado.");
            return "redirect:/";
        });
    }

    private String exibirFormulario(Model model, ColaboradorForm form, boolean edicao) {
        model.addAttribute("form", form);
        model.addAttribute("edicao", edicao);
        return "colaboradores/form";
    }
}