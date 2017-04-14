package de.udk.drl.mazirecorderandroid.utils;


import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.subjects.BehaviorSubject;

public class ObservableProperty<T> extends Observable<T> {

    private final BehaviorSubject<T> subject;
    private T value;

    public ObservableProperty() {
        subject = BehaviorSubject.create();
    }

    public ObservableProperty(T defaultValue) {
        this.value = defaultValue;
        subject = BehaviorSubject.create();
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
        subject.onNext(value);
    }

    public boolean isNull() {
        return value == null;
    }

    @Override
    protected void subscribeActual(Observer<? super T> observer) {
        subject.subscribe(observer);
    }
}