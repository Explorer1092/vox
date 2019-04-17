package com.voxlearning.utopia.admin.controller.mizar;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarBrand;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

/**
 * 机构导流品牌管理 By Wyc 2016-08-22
 */

@Controller
@RequestMapping("/mizar/brand")
public class CrmMizarBrandController extends CrmMizarAbstractController {

    private static final String LogoField = "logo";
    private static final String BrandPhotoField = "photo";
    private static final String CertificationField = "certification";
    private static final String FacultyField = "faculty";

    private static final List<String> PhotoType = Arrays.asList(LogoField, BrandPhotoField, CertificationField, FacultyField);
    private static final int BRAND_PAGE_SIZE = 10;


    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String brandIndex(Model model) {
        int page = getRequestInt("page", 1);
        page = page <= 0 ? 1 : page;
        String brandName = getRequestString("brand");
        Pageable pageable = new PageRequest(page - 1, BRAND_PAGE_SIZE);
        Page<MizarBrand> mizarBrands = mizarLoaderClient.loadBrandByPage(pageable, brandName);
        model.addAttribute("brandList", mizarBrands.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPage", mizarBrands.getTotalPages());
        model.addAttribute("hasPrev", mizarBrands.hasPrevious());
        model.addAttribute("hasNext", mizarBrands.hasNext());
        model.addAttribute("brand", brandName);
        return "mizar/brand/brandindex";
    }

    @RequestMapping(value = "info.vpage", method = RequestMethod.GET)
    public String brandInfo(Model model) {
        String brandId = getRequestString("bid");
        MizarBrand brand = mizarLoaderClient.loadBrandById(brandId);
        if (StringUtils.isNotBlank(brandId) && brand == null) {
            getAlertMessageManager().addMessageError("无效的品牌ID : " + brandId);
            return "mizar/brand/brandindex";
        }
        model.addAttribute("bid", brandId);
        model.addAttribute("new", brand == null);
        model.addAttribute("brand", brand);
        return "mizar/brand/brandinfo";
    }

    @RequestMapping(value = "savebrand.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveBrand() {
        String brandId = getRequestString("bid");
        String brandName = getRequestString("bname");
        String introduction = getRequestString("intro");
        String establishment = getRequestString("establish");
        String shopScale = getRequestString("scale");
        String certificationName = getRequestString("cerName");
        String points = getRequestString("points");
        try {
            MizarBrand brand = null;
            if (StringUtils.isNotBlank(brandId)) {
                brand = mizarLoaderClient.loadBrandById(brandId);
            }
            if (brand == null) {
                brand = new MizarBrand();
            }
            brand.setBrandName(brandName);
            brand.setIntroduction(introduction);
            brand.setEstablishment(establishment);
            brand.setShopScale(shopScale);
            brand.setCertificationName(certificationName);
            brand.setPoints(splitString(points));
            return mizarServiceClient.saveMizarBrand(brand);
        } catch (Exception ex) {
            logger.error("Save Mizar brand failed", ex);
            return MapMessage.errorMessage("保存品牌失败：" + ex.getMessage());
        }
    }

    @RequestMapping(value = "uploadphoto.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploadBrandPhoto() {
        String field = getRequestString("field");
        String brandId = getRequestString("bid");
        String desc = getRequestString("desc");
        String facultyName = getRequestString("fname");
        Integer facultyExp = getRequestInt("fexp");
        String facultyDesc = getRequestString("fdesc");
        if (!PhotoType.contains(field)) {
            return MapMessage.errorMessage("上传类型错误");
        }
        if (!FacultyField.equals(field) && StringUtils.isBlank(desc)) {
            return MapMessage.errorMessage("请填写描述");
        }
        if (FacultyField.equals(field)) {
            if (StringUtils.isBlank(facultyName)) {
                return MapMessage.errorMessage("请填写教师名称");
            }
            if (facultyExp <= 0) {
                return MapMessage.errorMessage("请填写正确的教龄");
            }
            if (StringUtils.isNotBlank(facultyDesc) && facultyDesc.length() > 5) {
                return MapMessage.errorMessage("描述不能超过5个字");
            }
        }
        try {
            MizarBrand brand = mizarLoaderClient.loadBrandById(brandId);
            if (brand == null) {
                return MapMessage.errorMessage("无效的品牌信息");
            }
            // 上传文件
            String fileName = uploadPhoto("file");
            if (StringUtils.isBlank(fileName)) {
                return MapMessage.errorMessage("文件上传失败");
            }
            switch (field) {
                case LogoField:
                    brand.setBrandLogo(fileName);
                    break;
                case BrandPhotoField:
                    List<String> photos = brand.getBrandPhoto() == null ? new ArrayList<>() : brand.getBrandPhoto();
                    photos.add(fileName);
                    brand.setBrandPhoto(photos);
                    break;
                case CertificationField:
                    List<String> certifications = brand.getCertificationPhotos() == null ? new ArrayList<>() : brand.getCertificationPhotos();
                    certifications.add(fileName);
                    brand.setCertificationPhotos(certifications);
                    break;
                case FacultyField:
                    List<Map<String, Object>> faculties = brand.getFaculty() == null ? new ArrayList<>() : brand.getFaculty();
                    Map<String, Object> faculty = new HashMap<>();
                    faculty.put(MizarBrand.FACULTY_NAME, facultyName);
                    faculty.put(MizarBrand.FACULTY_PHOTO, fileName);
                    faculty.put(MizarBrand.FACULTY_EXPERIENCE, facultyExp);
                    faculty.put(MizarBrand.FACULTY_DESCRIPTION, facultyDesc);
                    faculties.add(faculty);
                    brand.setFaculty(faculties);
                    break;
                default:
                    return MapMessage.errorMessage("上传类型错误");
            }
            return mizarServiceClient.saveMizarBrand(brand);
        } catch (Exception ex) {
            logger.error("Crm Upload Mizar brand photo failed, brand={}, type={}", brandId, field, ex);
            return MapMessage.errorMessage("上传失败:" + ex.getMessage());
        }
    }

    @RequestMapping(value = "deletephoto.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteBrandPhoto() {
        String field = getRequestString("field");
        String brandId = getRequestString("bid");
        String fileName = getRequestString("file");
        if (!PhotoType.contains(field)) {
            return MapMessage.errorMessage("图片类型错误");
        }
        try {
            MizarBrand brand = mizarLoaderClient.loadBrandById(brandId);
            if (brand == null) {
                return MapMessage.errorMessage("无效的品牌信息");
            }
            // 删除文件
            switch (field) {
                case LogoField:
                    brand.setBrandLogo("");
                    break;
                case BrandPhotoField:
                    List<String> photos = brand.getBrandPhoto();
                    if (CollectionUtils.isEmpty(photos) || !photos.contains(fileName)) {
                        return MapMessage.errorMessage("无效的图片信息");
                    }
                    photos.removeIf(fileName::equals);
                    brand.setBrandPhoto(photos);
                    break;
                case CertificationField:
                    List<String> certifications = brand.getCertificationPhotos();
                    if (CollectionUtils.isEmpty(certifications) || !certifications.contains(fileName)) {
                        return MapMessage.errorMessage("无效的图片信息");
                    }
                    certifications.removeIf(fileName::equals);
                    brand.setCertificationPhotos(certifications);
                    break;
                case FacultyField:
                    List<Map<String, Object>> faculties = brand.getFaculty();
                    if (CollectionUtils.isEmpty(faculties)) {
                        return MapMessage.errorMessage("无效的图片信息");
                    }
                    faculties.removeIf(faculty -> fileName.equals(faculty.get(MizarBrand.FACULTY_PHOTO)));
                    brand.setFaculty(faculties);
                    break;
                default:
                    return MapMessage.errorMessage("图片类型错误");
            }
            MapMessage msg = mizarServiceClient.saveMizarBrand(brand);
            if (msg.isSuccess()) {
                deletePhoto(fileName);
            }
            return msg;
        } catch (Exception ex) {
            logger.error("Delete Mizar brand photo failed, brand={}, type={}, file={}", brandId, field, fileName, ex);
            return MapMessage.errorMessage("删除失败：" + ex.getMessage());
        }
    }

    @RequestMapping(value = "editfaculty.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage editFaculty() {
        int index = getRequestInt("index");
        String brandId = getRequestString("bid");
        String facultyName = getRequestString("fname");
        Integer facultyExp = getRequestInt("fexp");
        String facultyDesc = getRequestString("fdesc");

        if (StringUtils.isBlank(facultyName)) {
            return MapMessage.errorMessage("请填写教师名称");
        }
        if (StringUtils.isNotBlank(facultyDesc) && facultyDesc.length() > 5) {
            return MapMessage.errorMessage("描述不能超过5个字");
        }

        try {
            MizarBrand brand = mizarLoaderClient.loadBrandById(brandId);
            if (brand == null) {
                return MapMessage.errorMessage("无效的品牌信息");
            }
            if (brand.getFaculty() == null || brand.getFaculty().size() < index) {
                return MapMessage.errorMessage("无效的品牌信息");
            }
            List<Map<String, Object>> faculties = brand.getFaculty();
            Map<String, Object> faculty = faculties.get(index);
            faculty.put(MizarBrand.FACULTY_NAME, facultyName);
            if (facultyExp > 0) {
                faculty.put(MizarBrand.FACULTY_EXPERIENCE, facultyExp);
            } else {
                faculty.put(MizarBrand.FACULTY_EXPERIENCE, null);
            }
            faculty.put(MizarBrand.FACULTY_DESCRIPTION, facultyDesc);
            return mizarServiceClient.saveMizarBrand(brand);
        } catch (Exception ex) {
            logger.error("Crm Edit Mizar faculty failed, brand={}, type={}", brandId, ex);
            return MapMessage.errorMessage("上传失败:" + ex.getMessage());
        }
    }

}
