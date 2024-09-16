package com.example.aicreditanalyzer.controller;

import com.example.aicreditanalyzer.model.*;
import com.example.aicreditanalyzer.repository.AnalysisRepository;
import com.example.aicreditanalyzer.repository.UserRepository;
import com.example.aicreditanalyzer.service.GPTApiService;
import org.springframework.beans.factory.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class AnalysisController {

	@Autowired
	private AnalysisRepository analysisRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private GPTApiService gptApiService;

	@GetMapping("/credit-analysis")
	public String creditAnalysis(Model model, Authentication authentication) {
	    User user = userRepository.findByEmail(authentication.getName());
	    List<Analysis> analyses = analysisRepository.findByUserId(user.getId());

	    analyses.removeIf(a -> !"analysis".equals(a.getType()));

	    model.addAttribute("analyses", analyses);
	    return "credit-analysis";
	}

	@PostMapping("/credit-analysis")
	public String performCreditAnalysis(@RequestParam("score") String score, Authentication authentication) {
	    User user = userRepository.findByEmail(authentication.getName());
	    String result = gptApiService.analyzeCredit(score);

	    Analysis analysis = new Analysis();
	    analysis.setType("analysis");
	    analysis.setInputData(score);
	    analysis.setResult(result);
	    analysis.setUser(user);
	    analysis.setCreatedAt(LocalDateTime.now());

	    analysisRepository.save(analysis);
	    return "redirect:/credit-analysis";
	}

	@GetMapping("/credit-prediction")
	public String creditPrediction(Model model, Authentication authentication) {
	    User user = userRepository.findByEmail(authentication.getName());
	    List<Analysis> analyses = analysisRepository.findByUserId(user.getId());

	    analyses.removeIf(a -> !"prediction".equals(a.getType()));

	    model.addAttribute("analyses", analyses);
	    return "credit-prediction";
	}

	@PostMapping("/credit-prediction")
	public String performCreditPrediction(@RequestParam("amount") String amount, @RequestParam("income") String income,
			Authentication authentication) {
		User user = userRepository.findByEmail(authentication.getName());
		String result = gptApiService.predictCredit(amount, income);

		Analysis analysis = new Analysis();
		analysis.setType("prediction");
		analysis.setAmount(amount);
		analysis.setIncome(income);
		analysis.setResult(result);
		analysis.setUser(user);
		analysis.setCreatedAt(LocalDateTime.now());

		analysisRepository.save(analysis);
		return "redirect:/credit-prediction";
	}

	@GetMapping("/analysis/edit/{id}")
	public String editAnalysis(@PathVariable("id") Long id, Model model, Authentication authentication) {
		User user = userRepository.findByEmail(authentication.getName());
		Analysis analysis = analysisRepository.findById(id).orElse(null);

		if (analysis != null && analysis.getUser().getId().equals(user.getId())) {
			model.addAttribute("analysis", analysis);
			return "edit-analysis";
		} else {
			return "redirect:/credit-analysis";
		}
	}

	@PostMapping("/analysis/update")
	public String updateAnalysis(@ModelAttribute("analysis") Analysis analysis, Authentication authentication) {
	    User user = userRepository.findByEmail(authentication.getName());
	    Analysis existingAnalysis = analysisRepository.findById(analysis.getId()).orElse(null);

	    if (existingAnalysis != null && existingAnalysis.getUser().getId().equals(user.getId())) {
	        existingAnalysis.setCreatedAt(LocalDateTime.now());

	        if ("prediction".equals(existingAnalysis.getType())) {
	            existingAnalysis.setAmount(analysis.getAmount());
	            existingAnalysis.setIncome(analysis.getIncome());

	            
	            String newResult = gptApiService.predictCredit(existingAnalysis.getAmount(), existingAnalysis.getIncome());
	            existingAnalysis.setResult(newResult);
	        } else if ("analysis".equals(existingAnalysis.getType())) {
	            existingAnalysis.setInputData(analysis.getInputData());


	            String newResult = gptApiService.analyzeCredit(existingAnalysis.getInputData());
	            existingAnalysis.setResult(newResult);
	        }

	        analysisRepository.save(existingAnalysis);
	    }
	    
	    if("prediction".equals(existingAnalysis.getType())) {
	    	return "redirect:/credit-prediction";
	    }else {
	    	return "redirect:/credit-analysis";
	    }
	}

	@GetMapping("/analysis/delete/{id}")
	public String deleteAnalysis(@PathVariable("id") Long id, Authentication authentication) {
		User user = userRepository.findByEmail(authentication.getName());
		Analysis analysis = analysisRepository.findById(id).orElse(null);

		if (analysis != null && analysis.getUser().getId().equals(user.getId())) {
			analysisRepository.delete(analysis);
		}
		return "redirect:/credit-analysis";
	}
}
