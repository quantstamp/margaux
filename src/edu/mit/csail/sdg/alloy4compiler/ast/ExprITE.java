/*
 * Alloy Analyzer 4 -- Copyright (c) 2006-2008, Felix Chang
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package edu.mit.csail.sdg.alloy4compiler.ast;

import java.util.Collection;
import edu.mit.csail.sdg.alloy4.Pos;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.ErrorSyntax;
import edu.mit.csail.sdg.alloy4.ErrorType;
import edu.mit.csail.sdg.alloy4.ErrorWarning;
import edu.mit.csail.sdg.alloy4.JoinableList;
import static edu.mit.csail.sdg.alloy4compiler.ast.Sig.SIGINT;
import static edu.mit.csail.sdg.alloy4compiler.ast.Type.EMPTY;

/**
 * Immutable; represents an if-then-else expression.
 *
 * <p> <b>Invariant:</b>  type!=EMPTY => (cond.mult==0 && left.mult==0 && right.mult==0)
 */

public final class ExprITE extends Expr {

    /** The position of the IMPLIES token. */
    public final Pos pos;

    /** The condition formula. */
    public final Expr cond;

    /** The then-clause. */
    public final Expr left;

    /** The else-clause (if this ExprITE expression is unambiguously typedchecked, then this field is never null) */
    public final Expr right;

    /** Caches the span() result. */
    private Pos span=null;

    /** {@inheritDoc} */
    @Override public Pos span() {
        Pos p=span;
        if (p==null) span = (p = cond.span().merge(right!=null ? right.span() : null).merge(left.span()));
        return p;
    }

    /** {@inheritDoc} */
    @Override public void toString(StringBuilder out, int indent) {
        if (indent<0) {
            out.append('(');
            cond.toString(out,-1);
            out.append(" => ");
            left.toString(out,-1);
            if (right!=null) { out.append(" else "); right.toString(out,-1); }
            out.append(')');
        } else {
            for(int i=0; i<indent; i++) { out.append(' '); }
            out.append("if-then-else with type=").append(type).append('\n');
            cond.toString(out, indent+2);
            left.toString(out, indent+2);
            if (right!=null) right.toString(out, indent+2);
        }
    }

    /** Constructs a ExprITE expression. */
    private ExprITE(Pos pos, Expr cond, Expr left, Expr right, Type type, JoinableList<Err> errs) {
        super(null,null, (cond.ambiguous || left.ambiguous || (right!=null && right.ambiguous)), type, 0, cond.weight+left.weight+(right!=null ? right.weight : 0), errs);
        this.pos=pos;
        this.cond=cond;
        this.left=left;
        this.right=right;
    }

    /** Returns true if we can determine the two expressions are equivalent; may sometimes return false. */
    @Override public boolean isSame(Expr obj) {
        while(obj instanceof ExprUnary && ((ExprUnary)obj).op==ExprUnary.Op.NOOP) obj=((ExprUnary)obj).sub;
        if (obj==this) return true;
        if (!(obj instanceof ExprITE)) return false;
        ExprITE x=(ExprITE)obj;
        return cond.isSame(x.cond) && left.isSame(x.left) && (right==null ? x.right==null : (x.right!=null && right.isSame(x.right)));
    }

    /**
     * Constructs a ExprITE expression.
     *
     * @param cond - the condition formula
     * @param left - the then-clause
     * @param right - the else-clause
     */
    public static Expr make(Pos pos, Expr cond, Expr left, Expr right) {
        if (right==null) {
            if (left.type.is_bool) return ExprBinary.Op.OR.make(pos, Pos.UNKNOWN, cond.not(), left);
            JoinableList<Err> errs = emptyListOfErrors;
            if (cond.mult != 0) errs = errs.append(new ErrorSyntax(cond.span(), "Multiplicity expression not allowed here."));
            if (left.mult != 0) errs = errs.append(new ErrorSyntax(left.span(), "Multiplicity expression not allowed here."));
            cond = cond.typecheck_as_formula();
            return new ExprITE(pos, cond, left, null, left.type, errs.join(cond.errors).join(left.errors));
        }
        JoinableList<Err> errs = emptyListOfErrors;
        if (cond.mult != 0) errs = errs.append(new ErrorSyntax(cond.span(), "Multiplicity expression not allowed here."));
        if (left.mult != 0) errs = errs.append(new ErrorSyntax(left.span(), "Multiplicity expression not allowed here."));
        if (right.mult != 0) errs = errs.append(new ErrorSyntax(right.span(), "Multiplicity expression not allowed here."));
        Type c=EMPTY;
        while(left.errors.isEmpty() && right.errors.isEmpty()) {
            Type a=left.type, b=right.type;
            c = a.unionWithCommonArity(b);
            if (a.is_int && b.is_int) c=Type.makeInt(c);
            if (a.is_bool && b.is_bool) c=Type.makeBool(c);
            if (c==EMPTY) {
                if (Type.SIGINT2INT) {
                    if (a.is_int && b.intersects(SIGINT.type)) { right=right.cast2int(); continue; }
                    if (b.is_int && a.intersects(SIGINT.type)) { left=left.cast2int(); continue; }
                }
                if (Type.INT2SIGINT) {
                    if (a.is_int && b.hasArity(1)) { left=left.cast2sigint(); continue; }
                    if (b.is_int && a.hasArity(1)) { right=right.cast2sigint(); continue; }
                }
                errs = errs.append(new ErrorType(cond.span().merge(right.span()).merge(left.span()),
                    "The then-clause and the else-clause must match.\nThe then-clause has type: "
                    + a + "\nand the else-clause has type: " + b));
            }
            break;
        }
        cond = cond.typecheck_as_formula();
        return new ExprITE(pos, cond, left, right, c, errs.join(cond.errors).join(left.errors).join(right.errors));
    }

    /** {@inheritDoc} */
    @Override public Expr resolve(Type p, Collection<ErrorWarning> warns) {
        if (errors.size()>0) return this;
        if (right==null) {
            Type a = left.type.intersect(p);
            if (p.is_int) a=Type.makeInt(a);
            if (p.is_bool) a=Type.makeBool(a);
            if (p.size()>0 && left.type.hasTuple() && !a.hasTuple()) warns.add(new ErrorWarning(left.span(),"This subexpression is redundant."));
            Expr cond = this.cond.resolve(Type.FORMULA, warns);
            Expr left = this.left.resolve(a, warns);
            Expr right = null;
            int arity = left.type.arity();
            if (arity>0) { right=Sig.NONE; while(arity>1) { right=right.product(Sig.NONE); arity--; } }
               else if (left.type.is_int) { right=ExprConstant.ZERO; }
               // in all other cases, that means "left" contains an error, so the following make() will generate an Expr with an embedded Err message
            return make(pos,cond,left,right);
        }
        Type a=left.type, b=right.type;
        if (p.size()>0) {
            a=a.intersect(p);
            b=b.intersect(p);
            if (p.is_int) { a=Type.makeInt(a); b=Type.makeInt(b); }
            if (p.is_bool) { a=Type.makeBool(a); b=Type.makeBool(b); }
            if (left.type.hasTuple() && !a.hasTuple()) warns.add(new ErrorWarning(left.span(),"This subexpression is redundant."));
            if (right.type.hasTuple() && !b.hasTuple()) warns.add(new ErrorWarning(right.span(),"This subexpression is redundant."));
        } else {
            a=p;
            b=p;
        }
        Expr cond = this.cond.resolve(Type.FORMULA, warns);
        Expr left = this.left.resolve(a, warns);
        Expr right = this.right.resolve(b, warns);
        return (cond==this.cond && left==this.left && right==this.right) ? this : make(pos,cond,left,right);
    }

    /** {@inheritDoc} */
    @Override Object accept(VisitReturn visitor) throws Err {
        if (!errors.isEmpty()) throw errors.get(0);
        return visitor.visit(this);
    }
}
