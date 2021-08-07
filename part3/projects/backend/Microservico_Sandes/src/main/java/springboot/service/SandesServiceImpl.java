package springboot.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import springboot.dto.AvaliacaoSandesDTO;
import springboot.dto.SandesDTO;
import springboot.dto.SandesDetailedDTO;
import springboot.exception.ResourceNotFoundException;
import springboot.repository.AvaliacaoRepository;
import springboot.repository.ComentarioRepository;
import springboot.repository.SandesRepository;
import utils.PropertiesObtain;
import springboot.model.Avaliacao;
import springboot.model.Comentario;
import springboot.model.Sandes;
import springboot.model.ValueObject.*;

@Service
@Transactional
public class SandesServiceImpl implements SandesService {

	@Autowired
	private SandesRepository sandesRepository;

	@Autowired
	private AvaliacaoRepository avalicaoRepository;

	@Autowired
	private ComentarioRepository comentarioRepository;

	@Override
	public HttpStatus createSandes(SandesDTO sandesDTO) {

		Sandes.Builder sandesBuilder = new Sandes.Builder();
		Sandes sandes = sandesBuilder.setDesignacao(new Designacao(sandesDTO.getDesignacao()))
				.setImagem(sandesDTO.getImagem()).setTitulo(new Titulo(sandesDTO.getTitulo()))
				.setQuantidade(new Quantidade(sandesDTO.getQuantidade())).build();

		sandesRepository.save(sandes);
		return HttpStatus.OK;
	}

	@Override
	public HttpStatus updateSandes(long id, SandesDTO sandesDTO) {
		Optional<Sandes> SandesDB = this.sandesRepository.findById(id);

		if (SandesDB.isPresent()) {
			Sandes sandesUpdate = SandesDB.get();
			if (sandesDTO.getDesignacao() != null)
				sandesUpdate.setDesignacao(new Designacao(sandesDTO.getDesignacao()));
			if (sandesDTO.getQuantidade() != 0)
				sandesUpdate.setQuantidade(new Quantidade(sandesDTO.getQuantidade()));
			if (sandesDTO.getImagem() != null)
				sandesUpdate.setImagem(sandesDTO.getImagem());
			if (sandesDTO.getTitulo() != null)
				sandesUpdate.setTitulo(new Titulo(sandesDTO.getTitulo()));
			sandesRepository.save(sandesUpdate);
			return HttpStatus.OK;
		} else {
			throw new ResourceNotFoundException("Sandwish not found with id : " + id);
		}
	}

	@Override
	public List<SandesDTO> getAllSandes() {
		List<Sandes> listaSandes = this.sandesRepository.findAll();
		List<SandesDTO> list=new ArrayList<>();
		for (Sandes sandes : listaSandes) {
			list.add(sandes.transformToDto());
		}
		return list;
	}

	@Override
	public List<SandesDetailedDTO> getAllSandesDetailed() {
		List<Sandes> listaSandes = this.sandesRepository.findAll();
		List<Avaliacao> listaAvaliacoes = this.avalicaoRepository.findAll();
		List<Comentario> listaComentarios = this.comentarioRepository.findAll();
		List<SandesDetailedDTO> listaSandesDetailed = new ArrayList<>();

		for (int i = 0; i < listaSandes.size(); i++) {
			String titulo = listaSandes.get(i).getTitulo().getTitulo();
			String descricao = listaSandes.get(i).getDesignacao().getDesignacao();
			String imagem = listaSandes.get(i).getImagem();
			List<String> listaComentariosSandes = new ArrayList<>();
			for (Comentario comentario : listaComentarios) {
				if (comentario.getSandwishId() == listaSandes.get(i).getId()) {
					listaComentariosSandes.add(comentario.getDesignacao().getDesignacao());
				}
			}
			List<AvaliacaoSandesDTO> listaAvaliacoesSandes = new ArrayList<>();
			for (Avaliacao avaliacao : listaAvaliacoes) {
				if (avaliacao.getSandesId() == listaSandes.get(i).getId()) {
					listaAvaliacoesSandes.add(new AvaliacaoSandesDTO(
							Integer.parseInt(PropertiesObtain.getPropertiesValue("rate.max"))
									* avaliacao.getNota().getNota() / avaliacao.getEscalaMax(),
							avaliacao.getComentario()));
				}
			}
			listaSandesDetailed.add(
					new SandesDetailedDTO(descricao, imagem, titulo, listaComentariosSandes, listaAvaliacoesSandes));
		}
		return listaSandesDetailed;
	}

	@Override
	public SandesDTO getSandesById(long sandesId) {
		Optional<Sandes> sandesDB = this.sandesRepository.findById(sandesId);
		if (sandesDB.isPresent()) {
			return sandesDB.get().transformToDto();
		} else {
			throw new ResourceNotFoundException("Sandes not found with id : " + sandesId);
		}
	}

	@Override
	public HttpStatus deleteSandes(long sandesId) {
		Optional<Sandes> sandesDB = this.sandesRepository.findById(sandesId);
		if (sandesDB.isPresent()) {
			this.sandesRepository.delete(sandesDB.get());
			return HttpStatus.OK;
		} else {
			throw new ResourceNotFoundException("Sandes not found with id : " + sandesId);
		}
	}

}
