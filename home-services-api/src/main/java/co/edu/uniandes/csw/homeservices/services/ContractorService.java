package co.edu.uniandes.csw.homeservices.services;

import co.edu.uniandes.csw.auth.provider.StatusCreated;
import static co.edu.uniandes.csw.auth.stormpath.Utils.getClient;
import java.util.List;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import co.edu.uniandes.csw.homeservices.api.IContractorLogic;
import co.edu.uniandes.csw.homeservices.api.ICustomerLogic;
import co.edu.uniandes.csw.homeservices.api.IReviewLogic;
import co.edu.uniandes.csw.homeservices.dtos.ContractorDTO;
import co.edu.uniandes.csw.homeservices.entities.ContractorEntity;
import co.edu.uniandes.csw.homeservices.converters.ContractorConverter;
import co.edu.uniandes.csw.homeservices.converters.ReviewConverter;
import co.edu.uniandes.csw.homeservices.dtos.SkillDTO;
import co.edu.uniandes.csw.homeservices.converters.SkillConverter;
import co.edu.uniandes.csw.homeservices.dtos.ReviewDTO;
import co.edu.uniandes.csw.homeservices.entities.ReviewEntity;
import static co.edu.uniandes.csw.homeservices.services.UserService.getCurrentCustomer;
import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.group.Group;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

/**
 * @generated
 */
@Path("/contractors")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ContractorService {
    
    private static final Logger LOGGER = Logger.getLogger(ContractorService.class);

    private static final String CONTRACTOR_GROUP_HREF = "https://api.stormpath.com/v1/groups/17sNxjYJEeYN8qDMfuBIbh";
    private static final String ADMIN_GROUP_HREF = "https://api.stormpath.com/v1/groups/rRAbN1pw2hLLj66xeAx4z";

    @Inject private IContractorLogic contractorLogic;
    @Inject private ICustomerLogic customerLogic;
    @Inject private IReviewLogic reviewLogic;
    @Context private HttpServletRequest req;
    @Context private HttpServletResponse response;
    @QueryParam("page") private Integer page;
    @QueryParam("maxRecords") private Integer maxRecords;
    @QueryParam("skillName") private String skillName;
    @QueryParam("experienceName") private String experienceDesc;
    @QueryParam("idServiceRequest") private Integer idServiceRequest;
    @QueryParam("idContractor") private Integer idContractor;

    /**
     * Obtiene la lista de los registros de Book.
     *
     * @return Colección de objetos de ContractorDTO cada uno con sus respectivos Review
     * @generated
     */
        public List<ContractorDTO> resultAdmin() {
        List<ContractorDTO> res = new ArrayList();
        res.clear();
        if (page != null && maxRecords != null) {
            this.response.setIntHeader("X-Total-Count", contractorLogic.countContractors());
            res = ContractorConverter.listEntity2DTO(contractorLogic.getContractors(page, maxRecords));
        }
        res = ContractorConverter.listEntity2DTO(contractorLogic.getContractors());
        return res;
    }
    
    public List<ContractorDTO> validateAccount(Account account) {
        List<ContractorDTO> res = new ArrayList();
        res.clear();
        for (Group gr : account.getGroups()) {
            switch (gr.getHref()) {
                case ADMIN_GROUP_HREF:
                    res = resultAdmin();                
                    break;
                case CONTRACTOR_GROUP_HREF:
                    Integer id = (int) account.getCustomData().get("contractor_id");
                    List<ContractorDTO> list = new ArrayList();
                    list.add(ContractorConverter.fullEntity2DTO(contractorLogic.getContractor(id.longValue())));
                    res = list;
                    break;
                default:
                    break;
            }
        }
        return res;
    }
            
    @GET
    public List<ContractorDTO> getContractors() {
        String accountHref = req.getRemoteUser();
        List<ContractorDTO> res = new ArrayList();
        res.clear();
        if (accountHref != null) {
            Account account = getClient().getResource(accountHref, Account.class);
            
            /**
             * Obtiene la lista de los registros de contractors los cuales tengan
             * dentro de sus skills alguno que coincida con los skill que se esperan
             * en el service request.
             *
             * @param serviceReqId
             * @return Colección de objetos de ContractorDTO
             */
            if (idServiceRequest != null && idServiceRequest != 0){
                return  ContractorConverter.listEntity2DTO(contractorLogic.getContractorsBySkillServiceReq(idServiceRequest));
            } else {
                LOGGER.log(Priority.ERROR, "El id del service request enviado es null o esta vacio" );
            }
            
            if (skillName != null && !"".equals(skillName)){     
                res = ContractorConverter.listEntity2DTO(contractorLogic.getContractorsBySkill(skillName));
            }else if(experienceDesc != null && ! "".equals(experienceDesc)){
                res = ContractorConverter.listEntity2DTO(contractorLogic.getContractorsByExperience(experienceDesc));
            }
            else {
                res = validateAccount(account);
            }
        }
        return res;          
    }

    /**
     * Obtiene los datos de una instancia de Book a partir de su ID.
     *
     * @param id Identificador de la instancia a consultar
     * @return Instancia de ContractorDTO con los datos del Book consultado y sus Review
     * @generated
     */
    @GET
    @Path("{id: \\d+}")
    public ContractorDTO getContractor(@PathParam("id") Long id) {
        return ContractorConverter.fullEntity2DTO(contractorLogic.getContractor(id));
    }

    /**
     * Se encarga de crear un book en la base de datos.
     *
     * @param dto Objeto de ContractorDTO con los datos nuevos
     * @return Objeto de ContractorDTO con los datos nuevos y su ID.
     * @generated
     */
    @POST
    @StatusCreated
    public ContractorDTO createContractor(ContractorDTO dto) {
        ContractorEntity entity = ContractorConverter.fullDTO2Entity(dto);
        return ContractorConverter.fullEntity2DTO(contractorLogic.createContractor(entity));
    }

    /**
     * Actualiza la información de una instancia de Book.
     *
     * @param id Identificador de la instancia de Book a modificar
     * @param dto Instancia de ContractorDTO con los nuevos datos.
     * @return Instancia de ContractorDTO con los datos actualizados.
     * @generated
     */
    @PUT
    @Path("{id: \\d+}")
    public ContractorDTO updateContractor(@PathParam("id") Long id, ContractorDTO dto) {
        ContractorEntity entity = ContractorConverter.fullDTO2Entity(dto);
        entity.setId(id);
        return ContractorConverter.fullEntity2DTO(contractorLogic.updateContractor(entity));
    }

    /**
     * Elimina una instancia de Book de la base de datos.
     *
     * @param id Identificador de la instancia a eliminar.
     * @generated
     */
    @DELETE
    @Path("{id: \\d+}")
    public void deleteContractor(@PathParam("id") Long id) {
        contractorLogic.deleteContractor(id);
    }

    /**
     * Obtiene una colección de instancias de SkillDTO asociadas a una
     * instancia de Contractor
     *
     * @param contractorId Identificador de la instancia de Contractor
     * @return Colección de instancias de SkillDTO asociadas a la instancia de Contractor
     * @generated
     */
    @GET
    @Path("{contractorId: \\d+}/skills")
    public List<SkillDTO> listSkills(@PathParam("contractorId") Long contractorId) {
        return SkillConverter.listEntity2DTO(contractorLogic.listSkills(contractorId));
    }

    /**
     * Obtiene una instancia de Skill asociada a una instancia de Contractor
     *
     * @param contractorId Identificador de la instancia de Contractor
     * @param skillId Identificador de la instancia de Skill
     * @generated
     */
    @GET
    @Path("{contractorId: \\d+}/skills/{skillId: \\d+}")
    public SkillDTO getSkills(@PathParam("contractorId") Long contractorId, @PathParam("skillId") Long skillId) {
        return SkillConverter.fullEntity2DTO(contractorLogic.getSkills(contractorId, skillId));
    }

    /**
     * Asocia un Skill existente a un Contractor
     *
     * @param contractorId Identificador de la instancia de Contractor
     * @param skillId Identificador de la instancia de Skill
     * @return Instancia de SkillDTO que fue asociada a Contractor
     * @generated
     */
    @POST
    @Path("{contractorId: \\d+}/skills/{skillId: \\d+}")
    public SkillDTO addSkills(@PathParam("contractorId") Long contractorId, @PathParam("skillId") Long skillId) throws Exception {
        return SkillConverter.fullEntity2DTO(contractorLogic.addSkills(contractorId, skillId));
    }

    /**
     * Remplaza las instancias de Skill asociadas a una instancia de Contractor
     *
     * @param contractorId Identificador de la instancia de Contractor
     * @param skills Colección de instancias de SkillDTO a asociar a instancia de Contractor
     * @return Nueva colección de SkillDTO asociada a la instancia de Contractor
     * @generated
     */
    @PUT
    @Path("{contractorId: \\d+}/skills")
    public List<SkillDTO> replaceSkills(@PathParam("contractorId") Long contractorId, List<SkillDTO> skills) {
        return SkillConverter.listEntity2DTO(contractorLogic.replaceSkills(contractorId, SkillConverter.listDTO2Entity(skills)));
    }

    /**
     * Desasocia un Skill existente de un Contractor existente
     *
     * @param contractorId Identificador de la instancia de Contractor
     * @param skillId Identificador de la instancia de Skill
     * @generated
     */
    @DELETE
    @Path("{contractorId: \\d+}/skills/{skillId: \\d+}")
    public void removeSkills(@PathParam("contractorId") Long contractorId, @PathParam("skillId") Long skillId) {
        contractorLogic.removeSkills(contractorId, skillId);
    }
    
    /**
     * Metodo que nos permite obtener los reviews que a tenido un contractor
     * recibiendo como parametro el id de este contractor
     * @param contractorId
     * @return List<ReviewDTO> si funciona bien, y null si ocurre un error
     */
    @GET
    @Path("{contractorId: \\d+}/reviews")
    public List<ReviewDTO> getReviews(@PathParam("contractorId") Long contractorId) {
        List<ReviewEntity> reviewsEntities = contractorLogic.getReviews(contractorId);
        return ReviewConverter.listEntity2DTO(reviewsEntities);
    }
    
    
       /**
     * Obtiene las calificaciones de un customer
     * @param customerId Identificador de la instancia de Customer
     * @return Colección de instancias de ReviewDTO asociadas a la instancia de Customer
     */
    @GET 
    @Path("{customerId: \\d+}/customerReviews ")
    public List<ReviewDTO> getCustomerReviews(@PathParam("customerId") Long customerId) {
        List<ReviewEntity> reviewsEntities = customerLogic.getReviews(customerId);
        return ReviewConverter.listEntity2DTO(reviewsEntities);
    }
    
     /**
     * Se encarga de crear un book en la base de datos.
     *
     * @param dto Objeto de ReviewDTO con los datos nuevos
     * @return Objeto de ReviewDTO con los datos nuevos y su ID.
     * @generated
     */
    @POST
    @Path("/reviews")
    @StatusCreated
    public ReviewDTO createReview(ReviewDTO dto) {
        ReviewEntity entity = ReviewConverter.basicDTO2Entity(dto);
        return ReviewConverter.basicEntity2DTO(reviewLogic.createReview(entity));
    }
    
    
}
