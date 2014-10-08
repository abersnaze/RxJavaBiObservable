package rx;

import rx.Observable.OnSubscribe;
import rx.functions.Action1;
import rx.functions.Action2;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.functions.Func3;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class BiObservable<T0, T1> {
    private BiOnSubscribe<T0, T1> f;

    public static interface DualOperator<R0, R1, T0, T1> extends Func1<DualSubscriber<? super R0, ? super R1>, DualSubscriber<? super T0, ? super T1>> {
    }

    public static interface BiOperator<R, T0, T1> extends Func1<Subscriber<? super R>, BiSubscriber<? super T0, ? super T1>> {
    }

    public static interface BiOnSubscribe<T0, T1> extends Action1<DualSubscriber<? super T0, ? super T1>> {
    }

    private BiObservable(BiOnSubscribe<T0, T1> f) {
        this.f = f;
    }

    public static <T0, T1> BiObservable<T0, T1> create(BiOnSubscribe<T0, T1> f) {
        return new BiObservable<T0, T1>(f);
    }

    public void subcribe(DualSubscriber<T0, T1> subscriber) {
        f.call(subscriber);
    }

    public <R0, R1> BiObservable<R0, R1> lift(final DualOperator<? extends R0, ? extends R1, ? super T0, ? super T1> dualOperator) {
        return BiObservable.create(new BiOnSubscribe<R0, R1>() {
            @Override
            public void call(DualSubscriber<? super R0, ? super R1> child) {
                f.call(dualOperator.call(child));
            }
        });
    }

    public <R> Observable<R> lift(final BiOperator<? extends R, ? super T0, ? super T1> biOperator) {
        return Observable.create(new OnSubscribe<R>() {
            @Override
            public void call(Subscriber<? super R> child) {
                f.call(biOperator.call(child));
            }
        });
    }

    public static <T0, T1> BiObservable<T0, T1> generateFirst(final Observable<? extends T1> ob1, final Func1<? super T1, ? extends T0> f) {
        return zip(ob1.map(f), ob1);
    }

    public static <T0, T1> BiObservable<T0, T1> zip(Observable<? extends T0> ob0, Observable<? extends T1> ob1) {
        return null;
    }

    public static <T0, T1, K> BiObservable<T0, T1> join(Observable<? extends T0> ob0, Func1<T0, K> keySelector0, Observable<? extends T1> ob1, Func1<T1, K> keySelector1) {
        return null;
    }

    public static final <T0, T1> BiObservable<T0, T1> combineLatest(Observable<? extends T0> o0, Observable<? extends T1> o1) {
        return null;
    }

    public static <T0, T1> BiObservable<T0, T1> product(final Observable<? extends T0> ob0, final Observable<? extends T1> ob1) {
        return create(new BiOnSubscribe<T0, T1>() {
            @Override
            public void call(final DualSubscriber<? super T0, ? super T1> subscriber) {
                final AtomicInteger active = new AtomicInteger(1);
                final AtomicBoolean error = new AtomicBoolean();

                ob0.subscribe(new Subscriber<T0>() {
                    @Override
                    public void onCompleted() {
                        if (active.decrementAndGet() == 0)
                            subscriber.onComplete();
                    }

                    @Override
                    public void onError(Throwable e) {
                        int last;
                        while (!active.compareAndSet(last = active.get(), 0))
                            ;
                        if (last != 0 && error.compareAndSet(false, true)) {
                            subscriber.onError(e);
                        }
                    }

                    @Override
                    public void onNext(final T0 t0) {
                        if (error.get())
                            return;
                        active.incrementAndGet();
                        ob1.subscribe(new Subscriber<T1>() {
                            @Override
                            public void onCompleted() {
                                if (active.decrementAndGet() == 0)
                                    subscriber.onComplete();
                            }

                            @Override
                            public void onError(Throwable e) {
                                int last;
                                while (!active.compareAndSet(last = active.get(), 0))
                                    ;
                                if (last != 0 && error.compareAndSet(false, true)) {
                                    subscriber.onError(e);
                                }
                            }

                            @Override
                            public void onNext(T1 t1) {
                                if (error.get())
                                    return;
                                subscriber.onNext(t0, t1);
                            }
                        });
                    }
                });
            }
        });
    }

    public static <T0, T1> BiObservable<T0, T1> just(T0 i0, Observable<? extends T1> ob1) {
        return product(Observable.just(i0), ob1);
    }

    public <R> BiObservable<R, T1> mapFirst(final Func2<? super T0, ? super T1, ? extends R> func) {
        return lift(new DualOperator<R, T1, T0, T1>() {
            @Override
            public DualSubscriber<? super T0, ? super T1> call(final DualSubscriber<? super R, ? super T1> child) {
                return new DualSubscriber<T0, T1>(child) {
                    @Override
                    public void onNext(T0 t0, T1 t1) {
                        child.onNext(func.call(t0, t1), t1);
                    }

                    @Override
                    public void onError(Throwable e) {
                        child.onError(e);
                    }

                    @Override
                    public void onComplete() {
                        child.onComplete();
                    }
                };
            }
        });
    }

    public <R> Observable<R> biMap(final Func2<? super T0, ? super T1, ? extends R> func) {
        return lift(new BiOperator<R, T0, T1>() {

            @Override
            public BiSubscriber<? super T0, ? super T1> call(final Subscriber<? super R> child) {
                return new BiSubscriber<T0, T1>(child) {

                    @Override
                    public void onNext(T0 t0, T1 t1) {
                        child.onNext(func.call(t0, t1));
                    }

                    @Override
                    public void onError(Throwable e) {
                        child.onError(e);
                    }

                    @Override
                    public void onComplete() {
                        child.onCompleted();
                    }
                };
            }
        });
    }

    public BiObservable<T0, T1> doOnNext(final Action2<T0, T1> action) {
        return lift(new DualOperator<T0, T1, T0, T1>() {

            @Override
            public DualSubscriber<? super T0, ? super T1> call(final DualSubscriber<? super T0, ? super T1> child) {
                return new DualSubscriber<T0, T1>(child) {

                    @Override
                    public void onNext(T0 t0, T1 t1) {
                        action.call(t0, t1);
                        child.onNext(t0, t1);
                    }

                    @Override
                    public void onError(Throwable e) {
                        child.onError(e);
                    }

                    @Override
                    public void onComplete() {
                        child.onComplete();
                    }
                };
            }
        });
    }

    public BiObservable<T0, T1> doOnNextFirst(final Action1<T0> action) {
        return lift(new DualOperator<T0, T1, T0, T1>() {

            @Override
            public DualSubscriber<? super T0, ? super T1> call(final DualSubscriber<? super T0, ? super T1> child) {
                return new DualSubscriber<T0, T1>(child) {

                    @Override
                    public void onNext(T0 t0, T1 t1) {
                        action.call(t0);
                        child.onNext(t0, t1);
                    }

                    @Override
                    public void onError(Throwable e) {
                        child.onError(e);
                    }

                    @Override
                    public void onComplete() {
                        child.onComplete();
                    }
                };
            }
        });
    }

    public BiObservable<T0, T1> doOnNextSecond(final Action1<T1> action) {
        return lift(new DualOperator<T0, T1, T0, T1>() {

            @Override
            public DualSubscriber<? super T0, ? super T1> call(final DualSubscriber<? super T0, ? super T1> child) {
                return new DualSubscriber<T0, T1>(child) {

                    @Override
                    public void onNext(T0 t0, T1 t1) {
                        action.call(t1);
                        child.onNext(t0, t1);
                    }

                    @Override
                    public void onError(Throwable e) {
                        child.onError(e);
                    }

                    @Override
                    public void onComplete() {
                        child.onComplete();
                    }
                };
            }
        });
    }

    public BiObservable<T0, T1> reduceFirst(final Func3<T0, T0, T1, T0> func) {
        return lift(new DualOperator<T0, T1, T0, T1>() {

            @Override
            public DualSubscriber<? super T0, ? super T1> call(final DualSubscriber<? super T0, ? super T1> subscriber) {
                final Map<T1, T0> seeds = new HashMap<T1, T0>();

                return new DualSubscriber<T0, T1>(subscriber) {
                    @Override
                    public void onNext(T0 t0, T1 t1) {
                        T0 seed = seeds.get(t1);
                        seeds.put(t1, (seed == null) ? t0 : func.call(seed, t0, t1));
                    }

                    @Override
                    public void onError(Throwable e) {
                        subscriber.onError(e);
                    }

                    @Override
                    public void onComplete() {
                        for (Entry<T1, T0> results : seeds.entrySet()) {
                            subscriber.onNext(results.getValue(), results.getKey());
                        }
                    }
                };
            }
        });
    }

    public <R> BiObservable<R, T1> reduceFirst(R seed, final Func3<R, T0, T1, R> func) {
        return lift(new DualOperator<R, T1, T0, T1>() {
            @Override
            public DualSubscriber<? super T0, ? super T1> call(final DualSubscriber<? super R, ? super T1> subscriber) {
                final Map<T1, R> seeds = new HashMap<T1, R>();

                return new DualSubscriber<T0, T1>(subscriber) {
                    @Override
                    public void onNext(T0 t0, T1 t1) {
                        R seed = seeds.get(t1);
                        seeds.put(t1, (seed == null) ? func.call(seed, t0, t1) : func.call(seed, t0, t1));
                    }

                    @Override
                    public void onError(Throwable e) {
                        subscriber.onError(e);
                    }

                    @Override
                    public void onComplete() {
                        for (Entry<T1, R> results : seeds.entrySet()) {
                            subscriber.onNext(results.getValue(), results.getKey());
                        }
                    }
                };
            }
        });
    }

    public <R> BiObservable<R, T1> composeFirst(final Func2<Observable<T0>, T1, Observable<R>> func) {
        return lift(new DualOperator<R, T1, T0, T1>() {
            @Override
            public DualSubscriber<? super T0, ? super T1> call(final DualSubscriber<? super R, ? super T1> subscriber) {
                final Map<T1, PublishSubject<T0>> foo = new HashMap<T1, PublishSubject<T0>>();

                return new DualSubscriber<T0, T1>() {
                    @Override
                    public void onNext(T0 t0, final T1 t1) {
                        PublishSubject<T0> subject = foo.get(t1);

                        if (subject == null) {
                            subject = PublishSubject.<T0> create();
                            foo.put(t1, subject);
                            func.call(subject, t1).subscribe(new Subscriber<R>() {
                                @Override
                                public void onCompleted() {
                                    // TODO
                                }

                                @Override
                                public void onError(Throwable e) {
                                    // TODO Auto-generated method stub
                                }

                                @Override
                                public void onNext(R r) {
                                    subscriber.onNext(r, t1);
                                }
                            });
                        }

                        subject.onNext(t0);
                    }

                    @Override
                    public void onError(Throwable e) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onComplete() {
                        // TODO Auto-generated method stub

                    }
                };
            }
        });
    }

    public <R> BiObservable<T0, R> mapSecond(Func2<? super T0, ? super T1, ? extends R> func) {
        return flip().mapFirst(flip(func)).flip();
    }

    private static <T0, T1, R> Func2<T1, T0, R> flip(final Func2<? super T0, ? super T1, ? extends R> func) {
        return new Func2<T1, T0, R>() {
            @Override
            public R call(T1 t1, T0 t0) {
                return func.call(t0, t1);
            }
        };
    }

    public BiObservable<T1, T0> flip() {
        return lift(new DualOperator<T1, T0, T0, T1>() {
            @Override
            public DualSubscriber<T0, T1> call(final DualSubscriber<? super T1, ? super T0> child) {
                return new DualSubscriber<T0, T1>(child) {
                    @Override
                    public void onNext(T0 t0, T1 t1) {
                        child.onNext(t1, t0);
                    }

                    @Override
                    public void onError(Throwable e) {
                        child.onError(e);
                    }

                    @Override
                    public void onComplete() {
                        child.onComplete();
                    }
                };
            }
        });
    }

}