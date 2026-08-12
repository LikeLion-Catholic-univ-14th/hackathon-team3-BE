package com.example.hackathon_team3_be.service;

import com.example.hackathon_team3_be.dto.*;
import com.example.hackathon_team3_be.entity.ProductEntity;
import com.example.hackathon_team3_be.entity.UnseenBagEntity;
import com.example.hackathon_team3_be.entity.UnseenLockEntity;
import com.example.hackathon_team3_be.exception.NoNegotiationMatchException;
import com.example.hackathon_team3_be.exception.UnseenNotFoundException;
import com.example.hackathon_team3_be.repository.ProductRepository;
import com.example.hackathon_team3_be.repository.UnseenBagRepository;
import com.example.hackathon_team3_be.repository.UnseenLockRepository;

import java.util.*;
import java.util.function.Function;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UnseenService {
    private static final Map<String,Integer> WEIGHTS = Map.of(
            "category",35,"color",25,"material",20,"size",20);
    private static final Set<String> LOCKABLE = Set.of(
            "category","color","material","size");
    private final ExperienceService experiences; private final UnseenBagRepository bags;
    private final ProductRepository products; private final UnseenLockRepository locks;

    public UnseenService(ExperienceService experiences, UnseenBagRepository bags,
                         ProductRepository products, UnseenLockRepository locks) {
        this.experiences=experiences; this.bags=bags; this.products=products; this.locks=locks;
    }

    public GenerateUnseenResponse generate(GenerateUnseenRequest r) {
        experiences.getRequired(r.experienceId());
        String id="UNSEEN_"+UUID.randomUUID().toString().replace("-","").substring(0,8).toUpperCase();
        BagAttributes a=new BagAttributes(or(r.category(),"unknown"),or(r.shape(),"unknown"),or(r.size(),"unknown"),
                or(r.favoriteColor(),"unknown"),or(r.material(),"unknown"),or(r.strap(),"unknown"),
                or(r.detail(),"unknown"),or(r.style(),"unknown"));
        UnseenBagEntity saved=bags.save(new UnseenBagEntity(id,r.experienceId(),"/generated/"+id+".jpg",a));
        return new GenerateUnseenResponse(id,saved.getImageUrl(),a);
    }

    public MatchResponse match(String id) {
        UnseenBagEntity bag=required(id);
        return new MatchResponse(id,rank(bag.attributes(),Set.of()));
    }

    @Transactional
    public LockFeaturesResponse lock(String id, LockFeaturesRequest r) {
        required(id);
        Set<String> normalized=new LinkedHashSet<>();
        for(String f:r.features()) { String n=f.toLowerCase(Locale.ROOT).trim();
            if(!LOCKABLE.contains(n)) throw new IllegalArgumentException("Unsupported lock feature: "+f);
            normalized.add(n);
        }
        locks.deleteByUnseenId(id); locks.flush();
        normalized.forEach(f->locks.save(new UnseenLockEntity(id,f)));
        return new LockFeaturesResponse(id,normalized);
    }

    public NegotiationResponse negotiate(String id) {
        UnseenBagEntity bag=required(id); Set<String> locked=locked(id);
        List<ProductMatch> ranked=rank(bag.attributes(),locked);
        if(ranked.isEmpty()) throw new NoNegotiationMatchException();
        ProductMatch best=ranked.get(0); ProductEntity product=products.findById(best.productId()).orElseThrow();
        Map<String,FeatureChange> changes=new LinkedHashMap<>();
        for(String f:WEIGHTS.keySet()) if(!locked.contains(f)) {
            String before=value(bag.attributes(),f), after=value(product.attributes(),f);
            if(!Objects.equals(before,after)) changes.put(f,new FeatureChange(before,after));
        }
        return new NegotiationResponse(id,locked,best,changes);
    }

    private List<ProductMatch> rank(BagAttributes a, Set<String> locked) {
        return products.findAll().stream().filter(p->locked.stream().allMatch(f->
                        known(value(a,f)) && known(value(p.attributes(),f))
                                && Objects.equals(value(a,f),value(p.attributes(),f))))
                .map(p->new ProductMatch(p.getProductId(),p.getName(),p.getImageUrl(),p.getPrice(),score(a,p)))
                .filter(p->p.matchScore()>0)
                .sorted(Comparator.comparingInt(ProductMatch::matchScore).reversed().thenComparing(ProductMatch::productId)).limit(10).toList();
    }
    static int score(BagAttributes a, ProductEntity product){
        BagAttributes b=product.attributes();
        return WEIGHTS.entrySet().stream()
                .filter(e->known(value(a,e.getKey())) && known(value(b,e.getKey())))
                .filter(e->Objects.equals(value(a,e.getKey()),value(b,e.getKey())))
                .mapToInt(Map.Entry::getValue).sum();
    }
    private Set<String> locked(String id){Set<String>s=new LinkedHashSet<>();locks.findByUnseenId(id).forEach(x->s.add(x.getFeature()));return s;}
    private UnseenBagEntity required(String id){return bags.findById(id).orElseThrow(()->new UnseenNotFoundException(id));}
    private static String value(BagAttributes a,String f){return switch(f){case"category"->a.category();case"size"->a.size();case"color"->a.color();case"material"->a.material();default->null;};}
    private static boolean known(String v){return v!=null&&!v.isBlank()&&!"unknown".equals(v);}
    private static String or(String v,String fallback){return v==null||v.isBlank()?fallback:v.toLowerCase(Locale.ROOT).trim();}
}
