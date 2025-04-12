package com.sullung2yo.seatcatcher.train.service;

import com.sullung2yo.seatcatcher.train.domain.SeatType;
import com.sullung2yo.seatcatcher.train.domain.TrainSeat;
import com.sullung2yo.seatcatcher.train.dto.request.TrainSeatRequest;
import com.sullung2yo.seatcatcher.train.repository.TrainSeatRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainSeatServiceImpl implements TrainSeatService {

    private final TrainSeatRepository trainSeatRepository;

    @Override
    public List<TrainSeat> findAllBySeatGroupId(Long id) {
        List<TrainSeat> result = trainSeatRepository.findAllByTrainSeatGroupId(id);
        if(result == null || result.isEmpty()) {
            throw new EntityNotFoundException("TrainSeat not found");
        }
        return result;
    }

    @Override
    public TrainSeat findById(Long id) {
        return trainSeatRepository.findById(id)
                .orElseThrow(()->new EntityNotFoundException("Train seat that id is(" + id + ")does not exist!"));
    }

    @Override
    @Transactional
    public void update(Long id, TrainSeatRequest seatInfo) {
        TrainSeat item = trainSeatRepository.findById(id)
                .orElseThrow(()->new EntityNotFoundException("Given train seat does not exist!"));

        Integer location = seatInfo.getSeatLocation();
        SeatType seatType = seatInfo.getSeatType();
        Integer jjimCount = seatInfo.getJjimCount();

        if(location != null) item.setSeatLocation(location);
        if(seatType != null) item.setSeatType(seatType);
        if(jjimCount != null) item.setJjimCount(jjimCount);

        trainSeatRepository.save(item);
    }
}
