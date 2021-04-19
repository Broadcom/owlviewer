package at.michaelgrath.owlviewer.frontend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @Value("${owlviewer.backend.url:@null}")
    private String backendUrl;

    @GetMapping({"/", "/index.html"})
    public String index(Model model) {
        model.addAttribute("backendUrl", backendUrl);
        return "index";
    }

}
