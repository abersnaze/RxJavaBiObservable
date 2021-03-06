package rx.internal.operators.dyad;

import rx.DyadSubscriber;
import rx.operators.DyadOperator;
import rx.functions.Func1;
import rx.functions.Func2;

public class OperatorMapDual<R0, R1, T0, T1> implements DyadOperator<R0, R1, T0, T1> {
    public static <R, T0, T1> DyadOperator<R, T1, T0, T1> singleMap1Operator(final Func1<? super T0, ? extends R> func) {
        return new OperatorMapDual<R, T1, T0, T1>(new TransformSubscriber<R, T1, T0, T1>() {
            @Override
            public void onNext(T0 t0, T1 t1) {
                getChild().onNext(func.call(t0), t1);
            }
        });
    }

    public static <R, T0, T1> DyadOperator<T0, R, T0, T1> singleMap2Operator(final Func1<? super T1, ? extends R> func) {
        return new OperatorMapDual<T0, R, T0, T1>(new TransformSubscriber<T0, R, T0, T1>() {
            @Override
            public void onNext(T0 t0, T1 t1) {
                getChild().onNext(t0, func.call(t1));
            }
        });
    }

    public static <R, T0, T1> DyadOperator<R, T1, T0, T1> dualMap1Operator(final Func2<? super T0, ? super T1, ? extends R> func) {
        return new OperatorMapDual<R, T1, T0, T1>(new TransformSubscriber<R, T1, T0, T1>() {
            @Override
            public void onNext(T0 t0, T1 t1) {
                getChild().onNext(func.call(t0, t1), t1);
            }
        });
    }

    public static <R, T0, T1> DyadOperator<T0, R, T0, T1> dualMap2Operator(final Func2<? super T0, ? super T1, ? extends R> func) {
        return new OperatorMapDual<T0, R, T0, T1>(new TransformSubscriber<T0, R, T0, T1>() {
            @Override
            public void onNext(T0 t0, T1 t1) {
                getChild().onNext(t0, func.call(t0, t1));
            }
        });
    }

    private static abstract class TransformSubscriber<R0, R1, T0, T1> extends DyadSubscriber<T0, T1> {
        private DyadSubscriber<? super R0, ? super R1> child;

        public void setChild(DyadSubscriber<? super R0, ? super R1> child) {
            this.child = child;
        }

        public DyadSubscriber<? super R0, ? super R1> getChild() {
            return this.child;
        }

        @Override
        public void onError(Throwable e) {
            child.onError(e);
        }

        @Override
        public void onComplete() {
            child.onComplete();
        }
    }

    private TransformSubscriber<R0, R1, T0, T1> subscriber;

    public OperatorMapDual(TransformSubscriber<R0, R1, T0, T1> subscriber) {
        this.subscriber = subscriber;
    }

    @Override
    public DyadSubscriber<? super T0, ? super T1> call(final DyadSubscriber<? super R0, ? super R1> child) {
        subscriber.setChild(child);
        return subscriber;
    }
}
